'use strict';
        define('trust-resource-url', [
          'angular'
        ], function (angular) {
          // define a new angular module named trust-resource-url
          // it will be added as angular module dependency to builtin 'cam.tasklist.custom' module
          // see the config.js entry above
          var module = angular.module('trust-resource-url', []);

          module.filter('trustUrl', function ($sce) {
              return function(url) {
                return $sce.trustAsResourceUrl(url);
              };
            });

          // it is not necessary to 'return' the customModule but it might come handy
          return module;
        });