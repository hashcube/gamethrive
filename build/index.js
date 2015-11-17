/* jshint node: true */

var path = require("path"),
  fs = require("fs"),
  wrench = require('wrench'),
  copyNotifyIcon = function (app, outputPath, tag, name) {
    'use strict';

    var destPath = path.join(outputPath, "res/drawable-" + tag +
        "dpi/ic_stat_gamethrive_default.png"),
      android = app.manifest.android,
      iconPath = android && android.icons && android.icons.alerts &&
        android.icons.alerts[name];

    wrench.mkdirSyncRecursive(path.dirname(destPath));

    if (iconPath && fs.existsSync(iconPath)) {
      fs.writeFileSync(destPath, fs.readFileSync(iconPath));
    }
  };

exports.onAfterBuild = function (devkitAPI, app, config, cb) {
  'use strict';

  if (config.target === 'native-android') {
    copyNotifyIcon(app, config.outputPath, "l", "low");
    copyNotifyIcon(app, config.outputPath, "m", "med");
    copyNotifyIcon(app, config.outputPath, "h", "high");
    copyNotifyIcon(app, config.outputPath, "xh", "xhigh");
    copyNotifyIcon(app, config.outputPath, "xxh", "xxhigh");
    copyNotifyIcon(app, config.outputPath, "xxxh", "xxxhigh");
  }

  cb();
};
