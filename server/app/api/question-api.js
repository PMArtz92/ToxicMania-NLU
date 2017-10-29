var express = require('express'),
    router = express.Router(),
    questionController = require('../controller/question-controller');



router.get('/NextQuestion',questionController.getNextQuestion);
router.get('/NextTenQuestion/',questionController.getNextTenQuestion);
router.get('/SessionQuestions/',questionController.getSessionQuestions);

// router.post('/postSkipQuestion',questionController.postSkipQuestion);

module.exports = router;
