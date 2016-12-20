var gulp = require('gulp');
var typescript = require('gulp-tsc');

gulp.task('default', function() {
  // place code for your default task here
});

gulp.task('compile', function(){
	var sources = [
		'appts.ts'
	]
	gulp.src(['appts.ts'])
		.pipe(typescript({module: "amd"}))
		.pipe(gulp.dest('./'))
});