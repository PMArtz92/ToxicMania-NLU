var gulp = require('gulp'),
    server = require('gulp-develop-server'),
    cssnano = require('gulp-cssnano'),
    uglify = require('gulp-uglify'),
    gulpIf = require('gulp-if'),
    useref = require('gulp-useref');

// run server
gulp.task('server:start', function () {
    server.listen({
        path: './server.js'
    });
});


// restart server if any changed
gulp.task('server:restart', function () {
    gulp.watch(['./server.js'], server.restart);
    gulp.watch(['./routes/*.js'], server.restart);
    gulp.watch(['../client/js/controllers/*.js'], server.restart);
    gulp.watch(['./app/api/*.js'], server.restart);
    gulp.watch(['./app/model/*.js'], server.restart);
    gulp.watch(['./app/controller/*.js'], server.restart);
});

gulp.task('useref', function () {
    return gulp.src('../web/index.html')
        .pipe(useref())
        // Minifies only if it's a JavaScript file
        .pipe(gulpIf('*.js', uglify()))
        // Minifies only if it's a CSS file
        .pipe(gulpIf('*.css', cssnano()))
        .pipe(gulp.dest('../dist'));
});

gulp.task('copy', function () {
    return gulp.src('../web/**/*')
        .pipe(gulp.dest('../dist'));
});


gulp.task('default', ['server:start', 'server:restart']);
