var gulp = require('gulp');
var typescript = require('gulp-tsc');
var concat = require('gulp-concat');

var SRC_ROOT = './src',
	TS_SERVER_SRC = [
		SRC_ROOT + '/server/*.ts'
	],

	TS_CLIENT_SRC = [
		SRC_ROOT + '/client/*.ts'
	],

	DEPLOY_DIR = 'compiled',
	DEPLOY_SERVER_DIR = DEPLOY_DIR + '/server',
	DEPLOY_CLIENT_DIR = DEPLOY_DIR + '/client',

	DEPLOY_SERVER_JS_NAME = 'server.js',
	DEPLOY_CLIENT_JS_NAME = 'main.js'
;

gulp.task('default', function() {
  // place code for your default task here
});

gulp.task('compile-client', function(){
	gulp.src(TS_CLIENT_SRC)
		.pipe(typescript({
			module: "commonjs",
			target: 'es6'
		}))
		.pipe(concat(DEPLOY_CLIENT_JS_NAME))
		.pipe(gulp.dest(DEPLOY_CLIENT_DIR))
});

gulp.task('compile-server', function(){
	gulp.src(TS_SERVER_SRC)
		.pipe(typescript({
			module: "commonjs",
			target: 'es6'
		}))
		.pipe(concat(DEPLOY_SERVER_JS_NAME))
		.pipe(gulp.dest(DEPLOY_SERVER_DIR))
});