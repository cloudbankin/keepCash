(function (module) {
    mifosX.controllers = _.extend(module, {
        CustomerController: function (scope, resourceFactory, location) {
            scope.clients = [];
            scope.actualClients = [];
            scope.searchText = "";
            scope.searchResults = [];
            scope.showClosed = false;
            scope.routeTo = function (id) {
                location.path('/viewcustomer/' + id);
            };

            scope.clientsPerPage = 15;

            scope.getResultsPage = function (pageNumber) {
                if(scope.searchText){
                    var startPosition = (pageNumber - 1) * scope.clientsPerPage;
                    scope.clients = scope.actualClients.slice(startPosition, startPosition + scope.clientsPerPage);
                    return;
                }
                var items = resourceFactory.clientCustomerResource.getAllClients({
                    offset: ((pageNumber - 1) * scope.clientsPerPage),
                    limit: scope.clientsPerPage
                }, function (data) {
                    scope.clients = data.pageItems;
                });
            }

            scope.initForCustomerPage = function () {

                var items = resourceFactory.clientCustomerResource.getAllClients({
                    offset: 0,
                    limit: scope.clientsPerPage
                }, function (data) {
                    scope.totalClients = data.totalFilteredRecords;
                    scope.clients = data.pageItems;
                });
            }


            scope.initForCustomerPage();

            scope.search = function () {
                scope.actualClients = [];
                scope.searchResults = [];
                scope.filterText = "";
                var searchString = scope.searchText;
                searchString = searchString.replace(/(^"|"$)/g, '');
                var exactMatch=false;
                var n = searchString.localeCompare(scope.searchText);
                if(n!=0)
                {
                    exactMatch=true;
                }

                if(!scope.searchText){
                    scope.initForCustomerPage();
                } else {
                    resourceFactory.globalSearch.search({query: searchString , resource: "customers,clientIdentifiers",exactMatch: exactMatch}, function (data) {
                        var arrayLength = data.length;
                        for (var i = 0; i < arrayLength; i++) {
                            var result = data[i];
                            var client = {};
                            client.status = {};
                            client.subStatus = {};
                            client.status.value = result.entityStatus.value;
                            client.status.code  = result.entityStatus.code;
                            if(result.entityType  == 'CLIENT'){

                                client.displayName = result.entityName;
                                client.accountNo = result.entityAccountNo;
                                client.id = result.entityId;
                                client.externalId = result.entityExternalId;
                                client.officeName = result.parentName;
                            }else if (result.entityType  == 'CLIENTIDENTIFIER'){
                                numberOfClients = numberOfClients + 1;
                                client.displayName = result.parentName;
                                client.id = result.parentId;
                                client.externalId = result.parentExternalId;

                            }
                            scope.actualClients.push(client);
                        }
                        var numberOfClients = scope.actualClients.length;
                        scope.totalClients = numberOfClients;
                        scope.clients = scope.actualClients.slice(0, scope.clientsPerPage);
                    });
                }
            }

        }
    });

    


    mifosX.ng.application.controller('CustomerController', ['$scope', 'ResourceFactory', '$location', mifosX.controllers.CustomerController]).run(function ($log) {
        $log.info("CustomerController initialized");
    });
}(mifosX.controllers || {}));