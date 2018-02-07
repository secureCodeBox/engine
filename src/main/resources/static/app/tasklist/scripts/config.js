var camTasklistConf = {
  // configure the date format
  // "dateFormat": {
  //   "normal": "LLL",
  //   "long":   "LLLL"
  // },
  //
    "locales": {
      "availableLocales": ["en", "de"],
      "fallbackLocale": "en"
    },

   // custom libraries and scripts loading and initialization,
   // see: http://docs.camunda.org/guides/user-guide/#tasklist-customizing-custom-scripts
   customScripts: {
//     // AngularJS module names
//     ngDeps: ['ui.bootstrap', ''],
//     // RequireJS configuration for a complete configuration documentation see:
//     // http://requirejs.org/docs/api.html#config
//     deps: ['jquery', 'custom-ui'],
//     paths: {
//       // if you have a folder called `custom-ui` (in the `scripts` folder)
//       // with a file called `scripts.js` in it and defining the `custom-ui` AMD module
//       'custom-ui': 'custom-ui/scripts'
//     }
        // names of angular modules defined in your custom script files.
        // will be added to the 'cam.tasklist.custom' as dependencies
        ngDeps: ['trust-resource-url'],

        // RequireJS modules to load.
        deps: ['trust-resource-url'],

        // RequreJS path definitions
        paths: {
            'trust-resource-url': './scripts/trust-resource-module/script'
        }
   }
};
