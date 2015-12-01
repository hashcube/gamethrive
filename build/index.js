/* jshint node: true */

var path = require("path"),
  fs = require("fs"),
  wrench = require('wrench'),
  copyNotifyIcon = function (app, outputPath, tag, name, large) {
    'use strict';

    var dest_path = path.join(outputPath, "res/drawable-" + tag +
        "dpi/"),
      android = app.manifest.android,
      icons = android && android.icons,
      icon_path;

    if (!icons) {
      return;
    }

    if (large) {
      icon_path = android.icons[name];
      dest_path += 'ic_onesignal_large_icon_default.png';
    } else {
      icon_path = android.icons.alerts && android.icons.alerts[name];
      dest_path += 'ic_stat_onesignal_default.png';
    }

    wrench.mkdirSyncRecursive(path.dirname(dest_path));

    if (icon_path && fs.existsSync(icon_path)) {
      fs.writeFileSync(dest_path, fs.readFileSync(icon_path));
    }
  };

exports.onBeforeBuild = function (devkitAPI, app, config, cb) {
  'use strict';

  var out_path = config.outputPath;

  if (config.target === 'native-android') {
    copyNotifyIcon(app, out_path, "xxxh", "192", true);
    copyNotifyIcon(app, out_path, "l", "low");
    copyNotifyIcon(app, out_path, "m", "med");
    copyNotifyIcon(app, out_path, "h", "high");
    copyNotifyIcon(app, out_path, "xh", "xhigh");
    copyNotifyIcon(app, out_path, "xxh", "xxhigh");
    copyNotifyIcon(app, out_path, "xxxh", "xxxhigh");
  }

  cb();
};
