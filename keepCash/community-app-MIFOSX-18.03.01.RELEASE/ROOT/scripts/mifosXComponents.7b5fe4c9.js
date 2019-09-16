define(['Q', 'underscore', 'mifosX'], function (Q) {
    var components = {
        models: [
            'models.5603b6b5'
        ],
        services: [
            'ResourceFactoryProvider',
            'HttpServiceProvider',
            'AuthenticationService',
            'SessionManager',
            'Paginator',
            'UIConfigService',
            'NotificationResponseHeaderProvider'
        ],
        controllers: [
            'controllers.b617cd0a'
        ],
        filters: [
            'filters.61c17440'
        ],
        directives: [
            'directives.9ddc8aa2'
        ]
    };

    return function() {
        var defer = Q.defer();
        require(_.reduce(_.keys(components), function (list, group) {
            return list.concat(_.map(components[group], function (name) {
                return group + "/" + name;
            }));
        }, [
            'routes-initialTasks-webstorage-configuration.8046bc7b'
        ]), function(){
            defer.resolve();
        });
        return defer.promise;
    }
});
