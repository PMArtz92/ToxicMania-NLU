var StudentSchedule = require('../model/student-schedule'),
    Schedule = require('../model/schedule'),
    Student = require('../model/student'),
    Company = require('../model/company'),
    Selectedstudentcompany = require('../model/selected-student-company'),
    Grid = require('../model/grid'),
    _ = require('lodash');


// Update student-company schedule
module.exports.updateSchedule = function (req, res, next) {

    var record = new Schedule(req.body);

    // Check if schedule exists
    Schedule.findOne({
        'student': record['student'],
        'company': record['company'],
        'position': record['position']
    }, function (err, result) {
        if (err) return next(err);
        if (result) {
            console.log("Updating schedule..."); // update info
            // Updating the schedule slot
            result.slot = record['slot'];
            result.save(function (err) {
                if (err) return next(err);
                res.json({
                    success: true,
                    msg: 'Schedule updated successfully'
                });
            });
        } else {
            console.log("Adding new schedule..."); // update info
            record.save(function (err) {
                if (err) return next(err);
                return res.json({
                    success: true,
                    msg: 'Schedule added successfully'
                });
            });
        }
    });

};

// Return schedule by student id
module.exports.getScheduleByStudent = function (req, res, next) {
    Schedule.find({
        'student': req.params.query
    }, function (err, result) {
        if (err) return next(err);
        var companies = _.map(result, 'company');
        Company.find({
            '_id': {
                $in: companies
            }
        }, function (err, result1) {
            if (err) return next(err);

            var times = ['10.30am - 10.45am', '10.45am - 11.00am', '11.00am - 11.15am', '11.15am - 11.30am',
                '11.30am - 11.45am', '11.45am - 12.00pm', '12.00pm - 12.15pm', '12.15pm - 12.30pm'];
            var slotsPerPanel = times.length;

            var final = [];
            // Loop through the two array results
            _.forEach(result1, function (compObj) {
                _.forEach(result, function (scheduleCompObj) {
                    // Check company ids and update company name in the returned object
                    if(scheduleCompObj.company == compObj._id){
                        var temp = {};
                        temp._id = scheduleCompObj._id;
                        temp.position = scheduleCompObj.position;
                        temp.company = compObj.name;
                        temp.student = scheduleCompObj.student;
                        temp.slot = scheduleCompObj.slot;
                        temp.slotTime = times[(scheduleCompObj.slot % slotsPerPanel) - 1];
                        final.push(temp);
                    }
                });
            });

            res.json({result: final});
        });
    });
};

// Return schedule by company
module.exports.getScheduleByCompany = function (req, res, next) {
    Schedule.find({
        'company': req.params.query
    }, function (err, result) {
        if (err) return next(err);
        var students = _.map(result, 'student');
        Student.find({
            'userID': {
                $in: students
            }
        }, function (err, result1) {
            if (err) return next(err);

            var final = [];
            // Loop through the two array results
            _.forEach(result1, function (student) {
                _.forEach(result, function (scheduleCompObj) {
                    // Check company ids and update company name in the returned object
                    if(scheduleCompObj.student == student.userID){
                        var temp = {};
                        temp.studentName = student.name;
                        temp.position = scheduleCompObj.position;
                        temp.slot = scheduleCompObj.slot;
                        temp.studentPhone = student.phone;
                        temp.stream = student.stream;
                        final.push(temp);
                    }
                });
            });

            res.json({success: true, result: final});
        });
    });
};


module.exports.deleteScheduleItem = function (req, res, next) {
    console.log(req.params.query);
    Schedule.findByIdAndRemove({
        '_id': req.params.query
    }, function (err, result) {
        if (err) return next(err);
        res.json({
            success: true,
            msg: 'Your schedule updated successfully'
        });
    });
};


module.exports.getShortlisted = function (req, res, next) {
    var finalResult = [];
    var finalStudentsOrderd = [];

    function totalSum(arr){
        var numbers = _.cloneDeep(arr);
        numbers.shift();
        var total = 0;
        _.forEach(numbers,function(num){
            total += num;
        });
        return total;
    }

    var allStudents = new Promise(function (resolve, reject) {
        Student.find({isAvailable: {$in: [null, true]}}, function (err, result) {
            if (result) {
                // console.log(result);
                resolve(result);
            }
            else {
                reject(err);
            }
        });
    });
    //
    var allCompanies = new Promise(function (resolve, reject) {
        Company.find({}, function (err, result) {
            if (result) {
                // console.log(result);
                resolve(result);
            }
            else {
                reject(err);
            }
        });
    });
    //
    Promise.all([allStudents, allCompanies])
        .then(function (results) {
            var allStudents = results[0];
            var allCompanies = results[1];
            var allSubqueries = [];

            allStudents.forEach(function (item) {

                var subPromise = new Promise(function (resolve, reject) {
                    Selectedstudentcompany.find({'student': item.userID}, function (err, result) {
                        if (err) reject(err);
                        var temp = [item];
                        var shortlistedCompany = {};
                        _.forEach(result, function (items) {
                            shortlistedCompany[items.company] = items;
                        });
                        allCompanies.forEach(function (company) {
                            // console.log(shortlistedCompany.hasOwnProperty(company._id));
                            if (shortlistedCompany.hasOwnProperty(company._id)) {
                                temp.push(0);
                            } else {
                                temp.push(-1);
                            }
                        });
                        finalResult.push(temp);
                        resolve(temp);
                    });
                });

                allSubqueries.push(subPromise);
            });

            Promise.all(allSubqueries).then(function functionName() {
                var sortedResult = _.sortBy(finalResult, [totalSum]);
                _.forEach(sortedResult,function(rows) {
                    finalStudentsOrderd.push(rows.shift());
                    //console.log(name.name);
                    //console.log(rows.toString());
                });

                return res.json({
                    success: true,
                    dataMatrix: sortedResult,
                    students: finalStudentsOrderd,
                    companies: allCompanies
                });
            }).catch(function (err) {
                console.log("Failed:", err);
                return next(err);
            });
        })
        .catch(function (err) {
            console.log("Failed:", err);
            return next(err);
        });
};


module.exports.saveGrid = function(req, res, next) {
    var grid = new Grid(req.body);
    console.log(grid);
    Grid.findOne({
    }, function (err, result) {
        if (result) {
            console.log("Updating grid info"); // update info
            result.data = grid.data;
            result.companies = grid.companies;
            result.students = grid.students;

            result.save(function (err) {
                if (err) return next(err);
                res.json({
                    success: true,
                    msg: 'Your details updated successfully'
                });
            });
        } else {
            grid.save(function (err) {
                if (err) return next(err);
                return res.json({
                    success: true,
                    msg: 'Grid added successfully'
                });
            });
        }
    });
};


module.exports.getSavedGrid = function(req, res, next) {
    Grid.findOne({}, function (err, result) {
        if(err) return next(err);
        //console.log(result);

        var parsedData = [];
        _.forEach(result.data,function(rows) {
            var temp2 = [];
            _.forEach(rows,function(i) {
                temp2.push(parseInt(i));
            });
            parsedData.push(temp2);
        });

        return res.json({
            success: true,
            dataMatrix: parsedData,
            students: result.students,
            companies: result.companies
        });
    });
};


// var sql = "INSERT INTO `user` (`U_Id`, `Email`, `Weight`, `Level`, `Mark`, `User_Name`) VALUES ('U126', '123@gmail.com', '123', '213123123', '12312', '21312') ON DUPLICATE KEY UPDATE `Weight` = 256789";
