var express = require('express'),
    router = express.Router(),
    multiplayController = require('../controller/multiplay-controller');


router.get('/gameSummary',multiplayController.getGameSummary);

router.get('/isgameStart',multiplayController.getisgameStart);

router.post('/connect',multiplayController.postConnect);
router.post('/disconnect',multiplayController.postDisconnect);
router.post('/newGame',multiplayController.postNewGame);
router.post('/startGame',multiplayController.postStartGame);

module.exports = router;
