const PORT = 3000;
const ROOTDIR = "root/";

const http = require('http');
const url = require('url');
const express = require('express');
const file = require('fs');

const logfile = "node.log";
const logging_buffer_delay = 4000;

var key = "";

const supportedMetrics = [
    "per_sequence_gc_content",
	"avg_gc_content",
    "per_base_n_content",
    "per_base_sequence_quality",
    "per_sequence_quality_scores",
    "sequence_length_distribution"
];

const db = require("./modules/database.js")(addToLoggingQueue, supportedMetrics);

var app = express()



// Check for commandline arguments
if (process.argv.indexOf("-build-database") >= 0) {
	db.build(function(success) { 
		if (success) { 
			console.log("Database built succesfully! :)"); 
			process.exit(0); 
		}
		console.log("Database failed! :("); 
		process.exit(1); 
	}); 
} else {
	startServer();
}

	
function startServer() {
	// log request
	app.use(function(req, res, next) {
		logRequest(req);
		next();
	});

	// Create server
	var server = http.createServer(app);
	server.listen(PORT, function(){
		console.log("Server running on port " + PORT);
	});

	// Enable static file access
	app.use(key, express.static(ROOTDIR));

	app.get("/status", function(req, res) {
		res.end("I'm OK");
	});
	
	// Allow inserting of rows into the database
	app.post(key + "/api/insert", function(req, res) {
		var body = '';
		req.on('data', function (data) { body += data; });

		req.on('end', function () {
			logRequest(req, body);
		   
			if (parsed = parseJson(body, res)) {
				db.insertAtOnce(supportedMetrics, parsed, function(stat, msg) { 
					res.status(stat);
					res.end(msg);
				})
			}
		});
	});

	// Allow selecting of rows from the database
	app.get(key + "/api/select", function(req, res) {
		var params = url.parse(req.url, true).query;
		
		db.select(req.params.quality, [params.offset, params.limit], [params.sort, params.order], function(data) {
			res.end(JSON.stringify(data));
		});
	});

	app.get(key + "/api/select/:quality/:date/:species", function(req, res) {
		var params = url.parse(req.url, true).query;
		
		if (!params.filter) params.filter = "{}";
		
		db.selectSamples(req.params.quality, [req.params.date, req.params.species], [params.sort, params.order], [params.offset, params.limit], parseJson(params.filter, res), function(data) {
			res.end(JSON.stringify(data));
		});
	});


	app.post(key + "/api/select/:metric", function(req, res) {	
		var body = '';
		req.on('data', function (data) { body += data; });

		req.on('end', function () {
			if (supportedMetrics.indexOf(req.params.metric) < 0) {
				res.status(401);
				res.end("Invalid Metric")
				console.log("Request for unsupported metric: " + req.params.metric);
				return;
			}
			if (parsed = parseJson(body, res)) {			
				db.selectmetric(req.params.metric, parsed, function(data) {
					res.end(JSON.stringify(data));
				});
			}
		});
	});
	
	app.post(key + "/api/historic/", function(req, res) { 
		var body = '';
		req.on('data', function (data) { body += data; });

		req.on('end', function () {
			db.selectHistoric(parseJson(body, res), function(err, data) { 
				if (err) {
					console.log(err);
					return res.end(500)
				}
				res.end(JSON.stringify(data)); 
			});
		});
	});

	app.post(key + "/api/download/", function(req, res) { 
		var body = '';
		req.on('data', function (data) { body += data; });

		req.on('end', function () {
			db.selectFullSampleData(parseJson(body, res), function(err, data) {
				if (err) {
					console.log(err);
					return res.end(500)
				}
				res.end(JSON.stringify(data)); 
			});
		});
	});
	
	app.get(key + "/api/distinct/:field", function(req, res) {
		db.selectDistinct(req.params.field, function(data) {
			res.end(JSON.stringify(data));
		});
	});
	
}

// log an incoming request
function logRequest(req, body) {
	var logline = req.method.toUpperCase() + " " + getCallerIP(req) 
		+ " ( " +  req.url + " )";
	
	if (body) { logline += ("    BODY: " + body); }
	
	addToLoggingQueue(logline);
}

// add line to list of lines to be logged to the log file
var loglines = "";
var logJob = false;
function addToLoggingQueue(line) {
	loglines += "\n" + new Date().toISOString() + ": " + line;

	if (logJob) { 
		clearTimeout(logJob);
	}
	
	logJob = setTimeout(function() {
		file.appendFile(logfile, loglines, function() {
			// log files written
		});
		
		loglines = "";
	}, logging_buffer_delay);
}

// Get the IP of the client making the request
// Source: https://stackoverflow.com/a/42092351/8464233
function getCallerIP(request) {
    var ip = request.headers['x-forwarded-for'] ||
        request.connection.remoteAddress ||
        request.socket.remoteAddress ||
        request.connection.socket.remoteAddress;
    ip = ip.split(',')[0];
    ip = ip.split(':').slice(-1); //in case the ip returned in a format: "::ffff:146.xxx.xxx.xxx"
    return ip;
}

// Parse JSON and return an error 400 if the JSON is invalid
function parseJson(string, res) {
	try {
        return JSON.parse(string);
    } catch (error) {
		if (res) {
			res.status(400)
			res.end("Malformed JSON");			
		}
		return false;
    }
}
