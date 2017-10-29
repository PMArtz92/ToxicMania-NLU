var express = require('express'),
    router = express.Router(),
    userController = require('../controller/user-controller');


router.get('/getUser/',userController.getUser);
router.post('/updateUser',userController.updateUser);
router.post('/newUser',userController.postnewUser);

module.exports = router;
