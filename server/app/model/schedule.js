var mongoose = require('mongoose');

// define the schema for student model
var scheduleSchema = mongoose.Schema({
    student: {
        type: String,
        required: true
    },
    company: {
        type: String,
        required: true
    },
    position: {
        type: String,
        required: true
    },
    slot: {
        type: Number,
        required: true
    }
});

module.exports = mongoose.model('Schedule', scheduleSchema);
