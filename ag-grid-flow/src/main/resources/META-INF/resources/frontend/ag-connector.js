import {InfiniteRowModelModule} from '@ag-grid-community/infinite-row-model';
import { Grid } from '@ag-grid-community/core';

import PolymerFrameworkComponentWrapper from "@ag-grid-community/polymer/src/PolymerFrameworkComponentWrapper.js";

/**
 * list to register html renderers
 */
if (typeof window.Vaadin.Flow.agGridRenderers === 'undefined') {
    window.Vaadin.Flow.agGridRenderers = [];
}


window.Vaadin.Flow.agGridConnector = {
    renderers: [],
    initLazy: function (c) {
        // Check whether the connector was already initialized
        if (c.$connector) {
            return;
        }

        c.$connector = {
            //
            currentPageParams : [],
            //// functions
            addItemClickListener : function() {

                this.agGrid.gridOptions.api.addEventListener('cellClicked', function myRowClickedHandler(event) {
                    c.$server.cellClicked(event.column.colDef.field, event.rowIndex,
                        {
                            screenX: event.event.screenX, screenY: event.event.screenY,
                            clientX: event.event.clientX, clientY: event.event.clientY,
                            detail: event.event.detail, button: event.event.button,
                            ctrlKey: event.event.ctrlKey, shiftKey: event.event.shiftKey,
                            altKey: event.event.altKey, metaKey: event.event.metaKey
                        });
                });
            },
            setColumnDefs : function(columnDefs) {
                // transform the configuration (name of the attribute cellRenderer)
                // to a javascript function
                columnDefs.forEach(el => { if (el.cellRenderer != null) {
                    console.log("ag-grid - cellRenderer {} ", el.cellRenderer);
                    el.cellRenderer = window.Vaadin.Flow.agGridRenderers[el.cellRenderer];
                    el.cellRendererFrameworkCallback = (coldId, rowId, actionName) => {
                        console.log("ag-grid - callback {} {} {}", coldId, rowId, actionName);
                        c.$server.cellRendererFrameworkCallback(coldId, rowId, actionName);
                    };
                }});
                // register the framework renderer polymer or lit
                // add cellRendererFrameworkCallback in the column configuration to call actions
                columnDefs.forEach(el => { if (el.cellRendererFramework != null) {
                    console.log("ag-grid - cellRendererFramework {} ", el.cellRendererFramework);
                    el.cellRendererFrameworkCallback = (coldId, rowId, actionName) => {
                        console.log("ag-grid - callback {} {} {}", coldId, rowId, actionName);
                        c.$server.cellRendererFrameworkCallback(coldId, rowId, actionName);
                    };
                }});

                this.agGrid.gridOptions.api.setColumnDefs(columnDefs);
            },
            setSelectionMode : function(rowSelection) {
                //debugger;
                this.agGrid.gridOptions.rowSelection = rowSelection;
                this.agGrid.gridOptions.api.addEventListener('selectionChanged', function selectionChangedHandler(event) {
                    c.$server.selectionChanged();
                });
                this.agGrid.gridOptions.api.addEventListener('rowSelected', function rowSelectedHandler(event) {
                    c.$server.rowSelected();
                });
            },

            //// https://www.ag-grid.com/javascript-grid-api/#scrolling
            scrollTo : function(rowIndex) {
                this.agGrid.gridOptions.api.ensureIndexVisible(rowIndex);
            },

            setRowCount : function(rowCount) {
                this.agGrid.gridOptions.api.setInfiniteRowCount(rowCount);
            },

            setDataSource : function() {
                var dataSource = {
                    rowCount: null, // behave as infinite scroll
                    getRows: function (params) {
                        // debugger;
                        console.log('ag-grid - getRows for {} to {} ', params.startRow, params.endRow);
                        /*
                        In vaadin 14.2, not yet implemented as 14.2 is brand new
                        let requestClientPagePromise = c.$server.requestClientPage(params.startRow, params.endRow, params.sortModel, params.filterModel);;
                        requestClientPagePromise.then(requestClientPage => params.successCallback(requestClientPage.page, requestClientPage.lastRow));
                         */
                        c.$server.requestClientPage(params.startRow, params.endRow, params.sortModel, params.filterModel);
                        c.$connector.currentPageParams[params.startRow] = params;
                    }
                };

                this.agGrid.gridOptions.api.setDatasource(dataSource);
            },

            setCurrentClientPage : function(rowsThisPage, lastRow, startRow) {
                if (c.$connector.currentPageParams[startRow] != null) {
                    c.$connector.currentPageParams[startRow].successCallback(rowsThisPage, lastRow);
                    c.$connector.currentPageParams[startRow] = null;
                }
            },

        };
        // let the grid know which columns and what data to use
        var gridOptions = {
            rowModelType : 'infinite',
            //cacheBlockSize: 30,
            // no impact on the client side
            //animateRows: true,
            // todo jcg by default its 100, no performance changes on the client side
           /* cacheBlockSize: 1000,*/
        };

        /** no scrolling issue with IE11 and use autopagination **/
        if (window.document.documentMode) {
            // Do IE stuff
            gridOptions.paginationAutoPageSize = true;
            //gridOptions.paginationPageSize = 30;
            gridOptions.pagination = true;
            //gridOptions.cacheBlockSize = 60;
        }

        let gridParams = {
            modules: [InfiniteRowModelModule],
            // add polymer and lit components available for renderer
            providedBeanInstances: {
                frameworkComponentWrapper: new PolymerFrameworkComponentWrapper()
            }
        };

        c.$connector.agGrid = new Grid(c, gridOptions, gridParams);
        /* cell editor does not work yet
        c.$connector.agGrid.gridOptions.api.addEventListener('cellValueChanged', function cellValueChangedHandler(event) {
            c.$server.cellValueChanged(event.column.colDef.field, event.rowIndex, event.oldValue, event.newValue);
        });*/

    }
}
