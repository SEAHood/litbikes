var gulp = require('gulp');
var typescript = require('gulp-tsc');
var concat = require('gulp-concat');
var clean = require('gulp-clean');
var runSequence = require('run-sequence');
var order = require("gulp-order");
var debug = require('gulp-debug');

var SRC_ROOT = './src',
	TS_SRC = [
		SRC_ROOT + '/references.ts',
		SRC_ROOT + '/game/game.ts',
		SRC_ROOT + '/dto.ts',
		SRC_ROOT + '/util.ts',
		SRC_ROOT + '/model/arena.ts',
		SRC_ROOT + '/model/bike.ts',
	],

	STATIC_ROOT = SRC_ROOT + '/static',
	STATIC_FILES = [
		STATIC_ROOT + '/**'
	],

	COMPILED_DIR = 'compiled',
	COMPILED_JS_DIR = COMPILED_DIR + '/js',
	COMPILED_FILES = COMPILED_DIR + '/**/*',

	JS_ORDER = [
		 '*.js',
		 'model/*.js',
		 'game/game.js',
	],

	WEBSERVER_DIR = '../server/web',
	WEBSERVER_FILES = WEBSERVER_DIR + '/**/*',

	DEPLOY_JS_NAME = 'litbikes.js'
;

gulp.task('build+deploy', function(callback) {
	runSequence(
		'clean',
		'build:ts',
		'move:static',
		'deploy'
	);
	callback();
});

gulp.task('clean', function() {
	return gulp.src([WEBSERVER_FILES, COMPILED_FILES], { read: false, allowEmpty: true })
		.pipe(clean({force: true}))
});

gulp.task('deploy', function() {
	return gulp.src(COMPILED_FILES)
		.pipe(gulp.dest(WEBSERVER_DIR))
});

gulp.task('move:static', function() {
	return gulp.src(STATIC_FILES, { base : './src/static' })
		.pipe(gulp.dest(COMPILED_DIR))
});

gulp.task('build:ts', function(){

	return gulp.src(TS_SRC)
		.pipe(typescript({
			module: "commonjs",
			target: 'es5'
		}))
		.pipe(order(JS_ORDER))
		.pipe(concat(DEPLOY_JS_NAME))
		.pipe(gulp.dest(COMPILED_JS_DIR))
});

/* TODO
gulp.task('bump-rev', function(){
	gulp.src(VERSION_FILE)
		.pipe(bump({type:'rev'}))
		.pipe(gulp.dest('./'));
});*/
