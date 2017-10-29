var express = require('express'),
    router = express.Router(),
    scheduleController = require('../controller/schedule-controller');

var jwt = require('express-jwt');
var config = require('../config/conf');
var auth = jwt({
    secret: config.secret,
    userProperty: 'payload'
});

router.get('/student_schedule/:query', auth, scheduleController.getScheduleByStudent);
router.get('/company_schedule/:query', auth, scheduleController.getScheduleByCompany);
router.post('/', auth, scheduleController.updateSchedule);
router.delete('/:query', auth, scheduleController.deleteScheduleItem);


router.get('/all', scheduleController.getShortlisted);
router.post('/saveGrid', scheduleController.saveGrid);
router.get('/getSavedGrid', scheduleController.getSavedGrid);

module.exports = router;
