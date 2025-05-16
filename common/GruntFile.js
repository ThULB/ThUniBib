module.exports = function (grunt) {
    grunt.loadNpmTasks('grunt-npmcopy');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    var path = require('path');
    var getAbsoluteDir = function (dir) {
        return path.isAbsolute(dir) ? dir : path.resolve(process.cwd(), dir);
    };
    grunt.initConfig({
        globalConfig: {
            assetsDirectory: getAbsoluteDir(grunt.option('assetsDirectory')),
            assetsDirectoryRelative: path.basename(grunt.option('assetsDirectory')),
            moduleDirectory: grunt.option('moduleDirectory')
        },
        npmcopy: {
            deps: {
                options: {
                    destPrefix: '<%=globalConfig.assetsDirectory%>/'
                },
                files: {
                    'apexcharts/dist': 'apexcharts/dist'
                },
            }
        },
    });

    grunt.registerTask('default', 'build assets directory', function () {
        grunt.task.run('npmcopy');
    });
};
