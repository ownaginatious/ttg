module.exports = function(grunt) {

    var serveStatic = require('serve-static');
    var proxySnippet = require('grunt-connect-proxy/lib/utils').proxyRequest;

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
            dev: {
                files: {
                    'dist/index.html': 'src/html/index.html',
                    'dist/listings.html': 'src/html/listings.html'
                }
            },
            prod: {
                files: [{
                    src: 'src/json/universities.json',
                    dest: 'dist/data/universities.json'
                }, {
                    expand: true,
                    cwd: 'bower_components/',
                    src: '**',
                    dest: 'dist/lib/'
                }, {
                    src: 'favicon.ico',
                    dest: 'dist/favicon.ico'
                }, {
                    expand: true,
                    cwd: 'src/css',
                    src: '**',
                    dest: 'dist/styles/'
                }]
            }
        },
        symlink: {
            dev: {
                files: [{
                    src: 'src/json/universities.json',
                    dest: 'dist/data/universities.json'
                }, {
                    src: 'bower_components/',
                    dest: 'dist/lib/'
                }, {
                    src: 'favicon.ico',
                    dest: 'dist/favicon.ico'
                }, {
                    src: 'src/js/ttg.js',
                    dest: 'dist/js/ttg.js'
                }, {
                    src: 'src/css/',
                    dest: 'dist/styles/'
                }]
            }
        },
        htmlmin: {
            prod: {
                options: {
                    removeComments: true,
                    collapseWhitespace: true
                },
                files: {
                    'dist/index.html': 'dist/index.html',
                    'dist/listings.html': 'dist/listings.html'
                }
            }
        },
        uglify: {
            prod: {
                files: {
                    'dist/js/ttg.js': 'src/js/ttg.js'
                }
            }
        },
        wiredep: {
            main: {
                options: {
                    overrides: {
                        bootstrap: {
                            "main": [
                                "dist/css/bootstrap.css",
                                "dist/js/bootstrap.js"
                            ]
                        }
                    },
                    directory: 'dist/lib/',
                    fileTypes: {
                        html: {
                            replace: {
                                js:  '<script type="text/javascript" src="static/{{filePath}}"></script>',
                                css: '<script type="text/css" src="static/{{filePath}}"></script>'
                            }
                        }
                    }
                },
                src: ['dist/index.html', 'dist/listings.html']
            }
        },
        watch: {
            dev: {
                files: ['src/**/*'],
                tasks: ['dev-build']
            }
        },
        connect: {
            dev: {
                options: {
                    hostname: '0.0.0.0',
                    port: '8000',
                    keepalive: true,
                    debug: true,
                    middleware: function (connect, options) {
                        return [
                            proxySnippet,
                            connect().use('/static', serveStatic('./dist')),
                            connect().use('/', serveStatic('./dist'))
                        ];
                    }
                },
                proxies: [
                    {
                        context: '/api/v1/schedule',
                        host: 'ttg-saved-schedules',
                        port: 8080,
                        rewrite: {
                            '^/api/v1/schedule': '/ttg'
                        }
                    },
                    {
                        context: '/api/',
                        host: 'ttg-web-backend',
                        port: 8080
                    }
                ]
            }
        },
        concurrent: {
            dev_serve: {
                tasks: ['watch:main', ['configureProxies:dev', 'connect:dev']],
                options: {
                    logConcurrentOutput: true
                }
            }
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
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-connect-proxy');
    grunt.loadNpmTasks('grunt-concurrent');

    grunt.registerTask('dev-build', ['bower', 'copy:dev', 'symlink:dev',
                                     'wiredep']);
    grunt.registerTask('prod-build', ['bower', 'clean', 'uglify:prod',
                                      'copy:prod', 'wiredep', 'htmlmin:prod']);
};
