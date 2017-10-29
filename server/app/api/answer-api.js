var express = require('express'),
    router = express.Router(),
    answerController = require('../controller/answer-controller');

// var jwt = require('express-jwt');
// var config = require('../config/conf');
// var auth = jwt({
//     secret: config.secret,
//     userProperty: 'payload'
// });
//
// // router.get('/student_schedule/:query', auth, scheduleController.getScheduleByStudent);
// // router.get('/company_schedule/:query', auth, scheduleController.getScheduleByCompany);
// // router.post('/', auth, scheduleController.updateSchedule);
// // router.delete('/:query', auth, scheduleController.deleteScheduleItem);
// //
// //
// // router.get('/all', scheduleController.getShortlisted);
// // router.post('/saveGrid', scheduleController.saveGrid);
// // router.get('/getSavedGrid', scheduleController.getSavedGrid);

// router.post('/',answerController.postAnswer);
router.get('/leaderBoard/',answerController.leaderBoard);
router.post('/Multiplay',answerController.postAnswerMultiplay);
router.post('/Singleplay',answerController.postAnswerSingleplay);
router.post('/',answerController.postAnswerMock);
module.exports = router;
