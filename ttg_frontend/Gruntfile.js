module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    clean: {
        src: ['dist/']
    },
    bower: {
      main: {
        options: {
          targetDir: 'bower_components/',
          layout: 'byType'
        }
      }
    },
    copy: {
      main: {
        files: {
          'dist/index.html': 'src/html/index.html',
          'dist/data/universities.json': 'src/json/universities.json'
        }
      }
    },
    symlink: {
      main: {
        files: {
          'dist/lib/': 'bower_components/',
          'dist/favicon.ico': 'favicon.ico',
          'dist/styles/': 'src/css/'
        }
      }
    },
    htmlmin: {
      main: {
        options: {
          removeComments: true,
          collapseWhitespace: true
        },
        files: {
          'dist/index.html': 'dist/index.html'
        }
      }
    },
    uglify: {
      main: {
        files: {
          'dist/js/ttg.min.js': 'src/js/ttg.js'
        }
      }
    },
    wiredep: {
      main: {
        options: {
          'directory': 'dist/lib/',
        },
        src: 'dist/index.html'
      }
    },
    watch: {
      files: ['src/**/*'],
      tasks: ['dev-build']
    }
  });

  // Load the plugin that provides the "uglify" task.
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-htmlmin');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-bower-task');
  grunt.loadNpmTasks('grunt-wiredep');
  grunt.loadNpmTasks('grunt-contrib-symlink');
  grunt.loadNpmTasks('grunt-contrib-watch');

  grunt.registerTask('dev-build', ['copy', 'wiredep']);
  grunt.registerTask('prod-build', ['bower', 'clean', 'symlink',
                               'uglify', 'copy', 'wiredep',
                               'htmlmin']);
};
