var gulp = require('gulp');
var typescript = require('gulp-tsc');
var concat = require('gulp-concat');
var clean = require('gulp-clean');
var runSequence = require('run-sequence');
var order = require("gulp-order");
var ts = require('gulp-typescript');
var series = require('stream-series');
var debug = require('gulp-debug');
var bump = require('gulp-bump');


var SRC_ROOT = 'src',
	/*TS_SRC = [
		SRC_ROOT + '/game/!*.ts',
		SRC_ROOT + '/references.ts',
		SRC_ROOT + '/!*.ts',
		SRC_ROOT + '/model/!*.ts',
	],*/
	TS_SRC = [
		SRC_ROOT + '/references.ts',
		SRC_ROOT + '/game/game.ts',
		SRC_ROOT + '/dto.ts',
		SRC_ROOT + '/util.ts',
		SRC_ROOT + '/model/arena.ts',
		SRC_ROOT + '/model/bike.ts',
	],

	VERSION_FILE = SRC_ROOT + 'version.json',



	CLIENT_LIBS = [
		'node_modules/socket.io-client/dist/socket.io.min.js',
		'node_modules/p5/lib/p5.min.js',
		'node_modules/underscore/underscore-min.js'
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
		'build-ts',
		['move-libs', 'move-html'],
		'deploy'
	);
	callback();
});

gulp.task('clean', function() {
	return gulp.src([WEBSERVER_FILES, COMPILED_FILES], {read: false})
		.pipe(clean({force: true}))
});

gulp.task('deploy', function() {
	return gulp.src(COMPILED_FILES)
		.pipe(gulp.dest(WEBSERVER_DIR))
});

gulp.task('move-libs', function() {
	return gulp.src(CLIENT_LIBS)
		.pipe(gulp.dest(COMPILED_JS_DIR))
});

gulp.task('move-html', function() {
	return gulp.src('src/index.html')
		.pipe(gulp.dest(COMPILED_DIR))
});

gulp.task('build-ts', function(){
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
