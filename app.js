var DEBUG = true;

// Other libraries
var http = require('http');
var url = require('url');
var assert = require('assert');
var queryString = require('querystring');
var crypto = require('crypto');
var MongoClient = require('mongodb').MongoClient;
var express = require('express');
var stringify = require('json-stable-stringify');

// Constants
const HOST = 'localhost';
const SERVER_PORT = 8080;
const SERVICE = 'michuservice';
const ID_LENGTH = 7;
const MAX_SIZE = 5000;

// Database related constants
const DATABASE = 'timetable_generator';
const COLLECTION = 'user_schedules';
const DB_PORT = 27017;
const DB_URL = 'mongodb://' + HOST + ':' + DB_PORT + '/' + DATABASE;

// Global variables
var db;

// Connect to MongoDB and start server
MongoClient.connect(DB_URL, function(err, database) {
    if (err) throw err;
    db = database;
    app.listen(SERVER_PORT);
});

var app = express();
app.use(function (err, req, res, next) {
    console.error(err.stack); 
    res.setHeader('Content-Type', 'application/json');
    res.status(500).send(JSON.stringify({ error: 'Something broke!' }, null, 2));
});

// Middleware to check date validity before passing to routes
app.use(function (req, res, next) {

    // If a date is in the query
    if (req.query.date) {
        // Try to parse it and set it in the request
        if (!isNaN(Date.parse(req.query.date))) {
            req.date = new Date(Date.parse(req.query.date));

        // If date is invalid, return error
        } else {
            var error = { 
                error: 'invalid date specified; please use iso 8601 format'
            }; 
            res.setHeader('Content-Type', 'application/json');
            return res.status(400).send(JSON.stringify(error, null, 2));
        }
    } 
    next();
});


// Routes
app
// GET
.get('/' + SERVICE + '/:id', function (req, res) {

    var id = req.params.id;
    try {
        // Fetch data from db and modify last_fetch_dt
        fetchJsonDocument(id, function(result) {
            // If a record was fetched, write back to client
            if (result) {
                var doc = {
                    data: result.data,
                }
                console.log('Writing JSON data to client...');
                res.setHeader('Content-Type', 'application/json');
                res.send(JSON.stringify(doc, null, 2)); // To prettify the JSON

            // Nothing was found for given id
            } else {
                console.log('No timetable found for id %s.', id)
                var jsonError = {
                    error: 'No timetable found for id ' + id
                };
                res.setHeader('Content-Type', 'application/json');
                return res.status(404).send(JSON.stringify(jsonError));
            }
        });
    } catch (err) {
        res.setHeader('Content-Type', 'application/json');
        return res.status(500).send(JSON.stringify({ error: err.message }));
    }
})

// POST
.post('/' + SERVICE, function (req, res) {
    var body = '';
    req.setEncoding('utf8');

    // Set listener to grab data and store in body
    req.on('data', function (chunk) {
        body += chunk;
    });

    // End event tells you that you have the entire body
    req.on('end', function() {
        // Grab JSON data from request
        try {
            var data = JSON.parse(body);
        } catch (err) {
            // Bad JSON
            var jsonError = {
                error: err.message
            };
            res.setHeader('Content-Type', 'application/json');
            return res.status(400).send(JSON.stringify(jsonError), null, 2);
        }

        // Check length
        if (JSON.stringify(data).length > MAX_SIZE) {
            // JSON is too long. Do not store and return error
            var jsonError = {
                error: 'Maximum payload size is 5000 characters'
            };
            res.setHeader('Content-Type', 'application/json');
            return res.status(413).send(JSON.stringify(jsonError), null, 2);
        }

        // Store new record in db
        try {
            insertJsonDocument(data, function(id) {
                res.setHeader('Content-Type', 'application/json');
                return res.send(JSON.stringify({ id: id }, null, 2));
            });
        } catch (err) {
            var jsonError = {
                error: err.message
            };
            res.setHeader('Content-Type', 'application/json');
            return res.status(400).send(JSON.stringify(jsonError, null, 2));
        }
    });
})

// DELETE
.delete('/' + SERVICE, function (req, res) {
    // If date was passed in
    if (req.date) {
        try {
            // Delete data that hasn't been accessed since the given date
            reapDocuments(req.date, function(n) {
                res.setHeader('Content-Type', 'application/json');
                res.send(JSON.stringify({ deleted_count : n }, null, 2));
            });
        } catch(err) {
            res.setHeader('Content-Type', 'application/json');
            res.status(500).send(JSON.stringify({ error: err.message }, null, 2));
        }
    // If no date specified, return error
    } else {
        var jsonError = { error: 'need to specify a date' };
        res.setHeader('Content-Type', 'application/json');
        res.status(400).send(JSON.stringify(jsonError, null, 2));
    }
});

// HELPER FUNCTIONS

// Function for storing JSON document
function insertJsonDocument(jsonData, callback) {
    var record = {
        data: jsonData,
        last_access_dt: new Date()
    };
    // Generate MD5 hash and check if this data is already stored
    var hash = generateHash(jsonData);
    record['hash'] = hash;
    db.collection(COLLECTION).find({hash: hash}).toArray(function (err, docs) {
        if (err) throw err;

        var id = null;
        for (var i = 0; i < docs.length; i++) {

            // If data already stored, return stored document's id
            if (stringify(docs[i].data) == stringify(jsonData)) {
                if (DEBUG) {
                    console.log('generated hash: ' + hash);
                    console.log('duplicate document found.');
                }
                fetchJsonDocument(docs[i].id); // Update last access date
                id = docs[i].id;
                break;
            }
        }

        // If a duplicate was found, just call callback with 
        // duplicate document's id
        if (id != null) {
            callback(id);

        // Otherwise, generate a random id and store the new document in db
        } else {
            // Generate a random id and set it as the record's id
            generateId(ID_LENGTH, function (id) {
                record['id'] = id;
                // Store record in db
                db.collection(COLLECTION).insertOne(record, function(err, result) {
                    if (err) throw err;
                    if (DEBUG) {
                        console.log('Stored JSON document (id=%s).', record['id']);
                    }
                    callback(record['id']);
                });
            });
        }
    }); // End db find query
}; // End insertJsonDocument function

// Function for fetching JSON document given an id
// Callback called with the result (either JSON doc or null) as its arg
function fetchJsonDocument(id, callback) {
    db.collection(COLLECTION).findOneAndUpdate(
        { id: id },
        { $currentDate: { last_access_dt: true } },
        function(err, result) {
            if (err) throw err;
            if (DEBUG) console.log('Fetching JSON document (id=%s).', id);
            
            if (callback) {
                if (result.value) {
                    if (DEBUG) {
                        console.log('JSON document:\n' + 
                        JSON.stringify(result.value, null, 2));
                    }

                    callback(result.value);
                
                // If no document with the given id is in the db, 
                // try fetching from the old workaround is.gd
                } else {
                    tryOldWorkAround(id, callback);
                }
            } 
        });
}; // End function fetchJsonDocument

// Function that tries is.gd for the schedule data
// is.gd was the old workaround where the schedules were being stored
function tryOldWorkAround(id, callback) {
    if (DEBUG) {
        console.log('JSON document (id=%s) was not found in db.', id);
        console.log('Attempting to retrieve schedule from "is.gd"');
    }
    var options = {
        host: 'is.gd',
        path: '/' + id
    };
    http.get(options, function (res) {

        // If is.gd gives a result, store in db and 
        // return the JSON data to the client
        if (typeof res.headers['location'] !== 'undefined') {
            var buf = new Buffer(res.headers['location'].replace(
                    'http://www.timetablegenerator.com/', ''),
                    'base64');
            if (DEBUG) console.log(buf.toString('ascii'));
            var jsonData = JSON.parse(buf.toString('ascii'));

            // Create new record to store in db
            var record = {
                id: id,
                data: jsonData,
                last_access_dt: new Date(),
                hash: generateHash(jsonData)
            };
            // Store JSON data from is.gd in the db
            try {
                db.collection(COLLECTION).insertOne(record, function(err, result) {
                    if (err) throw err;
                    if (DEBUG) console.log('Stored JSON document (id=%s).', record['id']);
                });
            } catch(err) {
                console.error('Unable to store JSON document (id=' 
                            + record['id'] + ') after fetching from is.gd');
                console.error(err.message);
                console.error(err.stack);
            }
            if (DEBUG) console.log('Successfully retrieved schedule from "is.gd"');
            callback({ data: jsonData });

        // If no result from is.gd, pass null back
        } else {
            if (DEBUG) console.log('Unable to retrieve schedule from "is.gd"');
            callback(null);
        }
    });
};

// Function for deleting JSON documents last accessed before the given date
// Callback called with the number of documents deleted as its argument.
function reapDocuments(date, callback) {
    console.log("Deleting documents that haven't been accessed since date: " 
            + date);
    db.collection(COLLECTION).deleteMany(
        { 
            last_access_dt: { 
                $lt: date
            }
        }, 
        function(err, result) {
            if (err) throw err;
            console.log(result.result.n + " documents deleted.");
            callback(result.result.n);
    });
};

// Function for generating MD5 hash of given JSON data
function generateHash(jsonData) {
    var md5sum = crypto.createHash('md5');
    md5sum.update(stringify(jsonData));
    return md5sum.digest('hex');
};

// Function for generating random id of a given length
// Callback will be called with the generated id provided as its argument
function generateId(idLength, callback) {
    var _sym = 'abcdefghijklmnopqrstuvwxyz1234567890';
    var id = '';

    // Generate an id of length 'idLength'
    for (var i = 0; i < idLength; i++) {
        id += _sym[parseInt(Math.random() * (_sym.length))];
    }

    // Check if id generated already exists in db
    db.collection(COLLECTION).find({id:id}, function(err, result) {
        // If generated id does not exist in db, use the id
        if (!result.length) {
            callback(id);

        // If generated id exists already in db, generate another id
        } else {
            generateId(count, callback);
        }
    });
};
