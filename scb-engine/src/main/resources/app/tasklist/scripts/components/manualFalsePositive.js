/*
 *
 *  SecureCodeBox (SCB)
 *  Copyright 2015-2018 iteratec GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

'use strict';
define('manual-false-positive', [
    'angular'
], function (angular) {
    // define a new angular module named trust-resource-url
    // it will be added as angular module dependency to builtin 'cam.tasklist.custom' module
    // see the config.js entry above
    var module = angular.module('manual-false-positive', []);

    module.directive('manualFalsePositive', function () {
        return {
            restrict: 'E',
            scope: {
                attributes: '=finding'
            },
            template: '<span class="label label-success" ng-show="!attributes.false_positive" ng-click="attributes.false_positive=true"><i aria-hidden="true" class="glyphicon glyphicon-ok-sign"></i> Ignore</span><span class="label label-warning" ng-show="attributes.false_positive" ng-click="attributes.false_positive=false"><i aria-hidden="true" class="glyphicon glyphicon-fire"></i> Unignore </span>'
        };
    });

    // it is not necessary to 'return' the customModule but it might come handy
    return module;
});
