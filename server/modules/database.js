const mysql = require('mysql2');
const fs = require('fs');

const auth_path = "auth.json";
const config_path = "config.json";

const db_reconnect_sleep = 4000;


// read config file
const config = JSON.parse(fs.readFileSync(config_path, "utf8").toString());
const maxScore = Object.keys(config.metrics).filter(el => !config.metrics[el].ignoreScore).length;

/**
 * Method to construct a default metric query which groups elements by their quality score.
 *
 * @param obj The object with the following fields:
 * 		metric:	the name of the metric, as stated in the Sample database column
 * 		table:  the name of the table
 * 		x:		the field to use as X variable
 * 		y: 		the field to use as Y variable
 * @returns {{SELECT: string, HISTORICMEAN: string, HISTORICRANGE: string}} The query
 */
function query(obj) {
	return {
			SELECT:	"SELECT `SampleID` AS ID,\
				`" + obj.x + "` AS x,\
				`" + obj.y + "` AS y,\
				`SampleName` AS name,\
				`" + obj.metric + "` AS quality\
				FROM `ultraqc_db`.`" + obj.table + "`\
				LEFT JOIN `ultraqc_db`.`samples` ON `ID` =  `SampleID`\
				WHERE #;",
			HISTORICMEAN:	"\
				SELECT `" + obj.x + "` AS x,\
					ROUND(AVG(s.`" + obj.y + "`), 3) AS y\
				FROM ultraqc_db.`" + obj.table + "` AS s\
				WHERE SampleID IN (\
					SELECT ID FROM ultraqc_db.samples\
					WHERE Species = ? AND #RANGE# #FILTERS#\
				) \
				GROUP BY `" + obj.x + "`\
				HAVING count(`" + obj.y + "`) > 10\;",
			HISTORICRANGE:	"\
				SELECT q.x, IF (q.mean - #MULT#*q.std > 0, q.mean - #MULT#*q.std, 0) AS low, q.mean + #MULT#*q.std AS high\
				FROM (	\
					SELECT `" + obj.x + "` AS x,\
						ROUND(AVG(s.`" + obj.y + "`), 3) AS mean,\
						ROUND(STD(s.`" + obj.y + "`), 3) AS std\
					FROM  ultraqc_db.`" + obj.table + "` AS s\
					WHERE SampleID IN (		\
						SELECT ID FROM ultraqc_db.samples\
						WHERE Species = ? AND #RANGE# #FILTERS#\
					) \
					GROUP BY `" + obj.x + "`\
					HAVING count(`" + obj.y + "`) > 10\
				) AS q\;"
	}
}

const QUERY = {
	PATH: {
		BUILD_DB: "sql/buildDatabase.sql"
	},
	QUERY: {
		INSERTION: {

			INSERT: "INSERT INTO `ultraqc_db`.`samples` (`Species`, `SampleName`, `SequencingTechnology`, `Coverage`, `Alignment`, `Duplication`, `Date`, `per_sequence_gc_content`, `avg_gc_content`, `per_base_n_content`, `per_base_sequence_quality`, `per_sequence_quality_scores`, `sequence_length_distribution`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			per_base_n_content: "INSERT INTO `ultraqc_db`.`metric_per_base_n_content` (`Base`, `N-Count`, `SampleID`) VALUES ?",
			per_sequence_gc_content: "INSERT INTO `ultraqc_db`.`metric_gc_content` (`Count`, `RelContent`, `GCcontent`, `SampleID`) VALUES ?",
			per_base_sequence_quality: "INSERT INTO `ultraqc_db`.`metric_per_base_sequence_quality` (`Base`, `Mean`, `Median`, `Lower_Quartile`, `Upper_Quartile`, `10th_Percentile`, `90th_Percentile`, `SampleID`) VALUES ?",
			per_sequence_quality_scores: "INSERT INTO `ultraqc_db`.`metric_per_sequence_quality_scores` (`Quality`, `Count`, `SampleID`) VALUES ?",
			sequence_length_distribution: "INSERT INTO `ultraqc_db`.`metric_sequence_length_distribution` (`Length`, `Count`, `SampleID`) VALUES ?"
		},
		SELECTION: {
			SELECT: "SELECT SQL_CALC_FOUND_ROWS s.`Species`, s.`SequencingTechnology`, DATE_FORMAT(s.`Date`, '%Y-%m-%d') AS `Date`, COUNT(*) AS Amount, `avg`, `bad`, (`avg` + `bad`) / count(*) AS qualitySort\
				FROM `ultraqc_db`.samples AS s\
				LEFT JOIN (\
					SELECT `Species`, DATE_FORMAT(`Date`, '%Y-%m-%d') AS `Date`, `SequencingTechnology`, COUNT(*) AS `avg`\
					FROM `ultraqc_db`.samples\
					WHERE #AVG_SELECTOR#\
					GROUP BY 1, 2, 3\
				) AS `w` ON w.Species = s.Species AND w.`Date` = s.`Date` AND w.`SequencingTechnology` = s.`SequencingTechnology`\
				LEFT JOIN (\
					SELECT `Species`, DATE_FORMAT(`Date`, '%Y-%m-%d') AS `Date`, `SequencingTechnology`, COUNT(*) AS `bad`\
					FROM `ultraqc_db`.samples\
					WHERE #BAD_SELECTOR#\
					GROUP BY 1, 2, 3\
				) AS `e` ON e.Species = s.Species AND e.`Date` = s.`Date` AND w.`SequencingTechnology` = s.`SequencingTechnology`\
				GROUP BY 1, 2, 3, 5, 6\
				ORDER BY #SORT#\
				LIMIT ?, ?;",
			SELECTSAMPLES: "SELECT SQL_CALC_FOUND_ROWS ID, SampleName, Species, DATE_FORMAT(s.`Date`, '%Y-%m-%d') AS `Date`, Coverage, Alignment, Duplication, #AVG_METRICS# AS avg, #BAD_METRICS# AS bad\
				FROM `ultraqc_db`.samples AS s\
				WHERE `Date` = ? AND `Species` = ? AND (#SELECTOR#)#FILTER#\
				ORDER BY #SORT#\
				LIMIT ?, ?;",
			SELECTFULLSAMPLE: "SELECT ID, SampleName AS Name, Species, DATE_FORMAT(`Date`, '%Y-%m-%d') AS `Date`\
				FROM ultraqc_db.samples\
				WHERE #;",
			SELECTSAMPLES: "\
				SELECT SQL_CALC_FOUND_ROWS *,\
					GREATEST(0, ROUND(10 \
						- (LENGTH(`avg`) - LENGTH(REPLACE(`avg`, ';', ''))) / (" + maxScore + "/5)\
						- (LENGTH(`bad`) - LENGTH(REPLACE(`bad`, ';', ''))) / (" + maxScore + "/10)\
					, 2)) AS score\
				FROM (SELECT ID, SampleName, Species, DATE_FORMAT(s.`Date`, '%Y-%m-%d') AS `Date`, Coverage, Alignment, Duplication, #AVG_METRICS# AS avg, #BAD_METRICS# AS bad\
					FROM `ultraqc_db`.samples AS s\
					WHERE `Date` = ? AND `Species` = ? AND (#SELECTOR#)#FILTER#\
				) AS `x`\
				#FILTER2#\
				ORDER BY #SORT#\
				LIMIT ?, ?;",
			TOTALROWS: "SELECT FOUND_ROWS() AS total;",
			DISTINCT: "SELECT DISTINCT `#`\
				FROM `ultraqc_db`.samples",
			per_sequence_gc_content: query({
					table: "metric_gc_content", metric: "per_sequence_gc_content",
					x: "Count", y: "RelContent"
			}),
			per_base_n_content: query({
					table: "metric_per_base_n_content", metric: "per_base_n_content",
					x: "Base", y: "N-Count"
			}),
			per_base_sequence_quality: query({
					table: "metric_per_base_sequence_quality", metric: "per_base_sequence_quality",
					x: "Base", y: "Mean",
					modifier: "SELECT MAX(per_base_sequence_quality) FROM `ultraqc_db`.`samples`"
			}),
			per_sequence_quality_scores: query({
					table: "metric_per_sequence_quality_scores", metric: "per_sequence_quality_scores",
					x: "Quality", y: "Count"
			}),
			sequence_length_distribution: query({
					table: "metric_sequence_length_distribution", metric: "sequence_length_distribution",
					x: "Length", y: "Count"
			}),
			avg_gc_content : {
				SELECT: "SELECT `ID`,\
				`SampleName` AS name,\
				`avg_gc_content` AS y\
				FROM `ultraqc_db`.samples\
				WHERE #;",
				HISTORICMEAN: "SELECT \
				ROUND(AVG(s.avg_gc_content), 3) AS y\
				FROM `ultraqc_db`.samples AS s\
				WHERE Species = ? AND #RANGE# #FILTERS#\;",
				HISTORICRANGE: "SELECT 1 AS x, 2 AS low, 3 as high"
			}			
		},
		QUALITY: {
			metric_quality: function(metric) {
				return "\
					SELECT `" + metric + "` AS value\
					FROM `ultraqc_db`.`samples`\
					WHERE #\
				"
			}
		}
	}
}

const util = require("./databaseUtil")(config);

module.exports = function(logger, supportedMetrics) {
	var exports = {};
	
	var mysql_connect = {
		host:"127.0.0.1",
		port: "3306",
		user: "root",
		password: "root"
	}


	// Establish MySQL connection, read from auth_path if it exists
	var conn;
	if (fs.existsSync(auth_path)) {
		var mysql_connect = JSON.parse(fs.readFileSync(auth_path, "utf8").toString());
	} else {
		console.log("WARNING: no '" + auth_path + "' file found, using default.");
	}


	var conn;
	
    /**
	 * handles reconnecting to database when connection is lost. ensures that the server does not crash
	 * when this happens.
	 * source: https://stackoverflow.com/a/20211143/8464233
     */
	function handleDisconnect() {
		// (re)create connection
		conn = mysql.createConnection(mysql_connect);

		// reconnect if connection failed
		conn.on('error', handleDbError);

		// try to connect
		conn.connect(function(err) {
			if (!err) { 
				console.log("Database connected!"); 
				if (typeof logger === "function") { logger("Database connected!"); }
			}
		});                                                                       

		
		function handleDbError(err) {
			if(err) {                                                                         
				console.log("Database error: " + err.code);
				if (typeof logger === "function") { logger("Database error: " + err.code); }
				setTimeout(handleDisconnect, db_reconnect_sleep); 
			}
		}
	}
	handleDisconnect();


	// build the database
	exports.build = function(callback) {
		if (!callback) { callback = function() {}; }
		
		console.log("Building database...");

		// Create connection that allows multiple queries at once
		var info_mult = mysql_connect;
		info_mult.multipleStatements = true;
		var db = mysql.createConnection(info_mult);
		db.connect();
		
		// Read in full query file for rebuilding the database
		fs.readFile(QUERY.PATH.BUILD_DB, "utf8", function(err, data) {
			if (err) { console.log(err); return callback(false); }
			
			db.query(data, function(e, res, cols) { 
				if (err) { console.log(err); return callback(false); }
				console.log("Database ready!");
				
				db.end();
				callback(true);
			});		
		});
	}


	// select all rows from the database
	exports.select = function(quality, params, sort, callback) {
		var query = QUERY.QUERY.SELECTION.SELECT
			.replace("#AVG_SELECTOR#", util.buildSelector("AVG"))
			.replace("#BAD_SELECTOR#", util.buildSelector("BAD"))
			.replace("#SORT#", "`" + sort[0] + "` " + sort[1]);
			
		conn.execute(query, [params[0], params[1]], function(err, data) {
			if (err) return util.ensureCallback(callback, 500, "Something went wrong", err);
			
			conn.execute(QUERY.QUERY.SELECTION.TOTALROWS, function(err, totalrows) {
				if (err) return util.ensureCallback(callback, 500, "Something went wrong", err);
				
				if (typeof callback === "function") { callback({total: totalrows[0].total, rows: data}); }
			});
		});
		
	}
	
	exports.selectSamples = function(quality, params, sort, limit, filter, callback) {
		// TODO: ensure quality exists
		var filterQuery = util.buildFilter(filter);
		var query = QUERY.QUERY.SELECTION.SELECTSAMPLES
			.replace("#SELECTOR#", util.buildSelector(quality.toUpperCase()))
			.replace("#AVG_METRICS#", util.buildQueryListOfMetrics("AVG"))
			.replace("#BAD_METRICS#", util.buildQueryListOfMetrics("BAD"))
			.replace("#SORT#", "`" + sort[0] + "` " + sort[1])
			.replace("#FILTER#", filterQuery[0])
			.replace("#FILTER2#", filterQuery[2]);

		conn.execute(query, params.concat(filterQuery[1], filterQuery[3], limit), function(err, data) {
			if (err) return util.ensureCallback(callback, 500, "Something went wrong", err);

			conn.execute(QUERY.QUERY.SELECTION.TOTALROWS, function(err, totalrows) {
				if (err) return util.ensureCallback(callback, 500, "Something went wrong", err);
				
				if (typeof callback === "function") { callback({total: totalrows[0].total, rows: data}); }
			});
		});
	}
		
		
	// selects a certain metric to be displayed
	exports.selectmetric = function(metric, rows, callback) {	
		var query = util.buildQuery(QUERY.QUERY.SELECTION[metric].SELECT, rows);
		conn.execute(query.query, query.params, function(err, data) {
			if (err) return util.ensureCallback(callback, 500, "Something went wrong", err);
			
			var obj = {series: []};
			if (config.metrics[metric].type == "column") {
				obj.categories = [];
				var values = [];
				
				for (var i = 0; i < data.length; i++) {
					values.push(data[i].y);
					obj.categories.push(data[i].name);
				}
				
				obj.series.push(new ColumnSeries(values)); 
			} else {
				var metrics = util.groupBy(data, "ID");
	
				for (var key in metrics) {	
					var dataset = metrics[key];
					var coordinates = metrics[key].map(function (el) { return {x: el.x, y: el.y}});
					obj.series.push(new Series(dataset[0].quality, dataset[0].name, coordinates, metric));
				}
			}
			// convert MySQL data to a list of datasets for the graphs
			
			
			var query = util.buildQuery(QUERY.QUERY.QUALITY.metric_quality(metric), rows);
			conn.execute(query.query, query.params, function(err, counts) {
				if (err) return util.ensureCallback(callback, 500, "Something went wrong", err);
				
				if (typeof callback === "function") {
					obj.counts = util.countQuality(counts, metric);
					callback(obj);
				}
			});
		});
	}
	
	// selects historic data according to species and date
	exports.selectHistoric = function(params, callback) {
		var histMeans = {}
		var histRanges = {}
		
		var multiplier = params.deviationMultiplier ? params.deviationMultiplier : 2;
		
		var means = Object.keys(config.metrics).map(metric => new Promise((resolve, reject) => {
			var queryMean = util.buildHistoric(QUERY.QUERY.SELECTION[metric].HISTORICMEAN, metric, params);
			
			queryMean[1].unshift(params.species);
			conn.execute(queryMean[0], queryMean[1], function(err, data) {
				if (err) return reject(err);
				
				histMeans[metric] = new HistoricSeries(metric, data);
				resolve();
			});
		}));
		
		var ranges = Object.keys(config.metrics).map(metric => new Promise((resolve, reject) => {
			var base = QUERY.QUERY.SELECTION[metric].HISTORICRANGE.split("#MULT#").join(multiplier);
			var queryRange = util.buildHistoric(base, metric, params);
			
			queryRange[1].unshift(params.species);
			conn.execute(queryRange[0], queryRange[1], function(err, data) {
				if (err) return reject(err);
				
				histRanges[metric] = new HistoricRange(metric, data);
				
				resolve();
			});
		}));
		
		var series = {histMeans, histRanges};
		
		Promise.all(means.concat(ranges)).then(() => callback(false, series), err => callback(err));
	}
	
	exports.selectDistinct = function(field, callback) {
		conn.execute(QUERY.QUERY.SELECTION.DISTINCT.replace("#", field), function(err, data) {
			if (err) return util.ensureCallback(callback, 500, "Something went wrong", err);
			
			if (typeof callback === "function") { callback(data); }
		});
	}
	
	exports.selectFullSampleData = function(selection, callback) {
		var selector = selection.map(() => "ID = ?").join(" OR ");
		
		var query = QUERY.QUERY.SELECTION.SELECTFULLSAMPLE.replace("#", selector);
		conn.execute(query, selection, function(err, data) {
			if (err) return util.ensureCallback(callback, 500, "Something went wrong", err);
			
			if (typeof callback === "function") { callback(false, data); }
		});
	}


	// insert at once format
	// source for promise usage: https://stackoverflow.com/a/18983245
	exports.insertAtOnce = function(graphs, data, callback) {
		console.log("Inserting new sample data...");
		console.time("Inserted " + data.samples.length + " samples in");
		
		conn.prepare(QUERY.QUERY.INSERTION.INSERT, function(err, statement) {
			if (err) return util.ensureCallback(callback, 500, "Something went wrong", err);
			
			let requests = data.samples.map((item) => {
				return new Promise((resolve, reject) => {
					statement.execute([
						data.species,
						item.sampleName,
						data.seqTech,
						item.coverage,
						item.alignment,
						item.duplication,
						data.date,
						item.metrics.per_sequence_gc_content.classification,
						item.avgGcContent,
						item.metrics.per_base_n_content.classification,
						item.metrics.per_base_sequence_quality.classification,
						item.metrics.per_sequence_quality_scores.classification,
						item.metrics.sequence_length_distribution.classification
					], function(err, data) {
						if (err) {
							reject([callback, 500, "Something went wrong", err]);
							return;
						}
						
						graphs.forEach(graph => {
							if (item.metrics !== undefined) {
								if (item.metrics[graph]) {
									item.metrics[graph].data.forEach(el => {
										el.push(data.insertId);
									})
								}
							}
						})
						
						// indicate this query was completed
						resolve();
					});
				});
			});
			
			// when all samples have completed, insert the metrics
			Promise.all(requests).then(() => {
				if (data.samples.length == 0) {
					console.timeEnd("Inserted " + data.samples.length + " samples in");
					return util.ensureCallback(callback, 204, "No data inserted", err);
				}
				console.timeEnd("Inserted " + data.samples.length + " samples in");
				graphs.forEach(graph => {
					if(!QUERY.QUERY.INSERTION[graph]) { return }
					if (data.samples[0].metrics === undefined) { console.log("No metrics found"); return; }
					if (data.samples[0].metrics[graph] === undefined) { console.log("no rows for "+ graph); return; }
					insertMetrics(data.samples, [graph], QUERY.QUERY.INSERTION[graph], callback);
				})
				if (typeof callback === "function") { util.ensureCallback(callback, 201, "Inserted " + data.samples.length + " samples", err); }
			}, function(obj) {
				return util.ensureCallback(obj[0], obj[1], obj[2], obj[3]);
			});
		});
	};
	
	/**
	 * Method to insert a metric into the database.
	 *
     * @param samples The array of samples of which metrics must be added
     * @param metric The name of the metric to insert
     * @param query The query to use for insertion
     * @param callback The callback function
     */
	function insertMetrics(samples, metric, query, callback) {
		var merged = util.merge(samples, el => el.metrics[metric].data);
		
		conn.query(query, [merged], function(err) {
			if (err) util.ensureCallback(callback, 500, "Something went wrong", err);
			
			console.log("Inserted " + merged.length + " rows for metric " + metric);
		});
	}

	return exports;
}

/**
 * class to represent a data series object.
 */
class Series {
	constructor(classification, name, data, metric) {
		this.quality = util.getQuality(classification, metric);
		this.color = config.colours[this.quality];
		this.name = name;
		this.data = data;
	}
}

class ColumnSeries {
	constructor(values) {
		this.data = values;
		this.type = "column";
	}
}

/**
 * class to represent the mean line of the data.
 */
class HistoricSeries {
	constructor(metric, data) {
		this.data = data;
		this.name = metric;
		this.color = config.colours.HIS;
		this.hisLine = true;
	}
}

/**
 * class to represent the range of the data.
 */
class HistoricRange {
	constructor(metric, data) {
		this.data = data;
		this.name = metric;
		this.fillColor = config.colours.RAN;
		this.hisLine = true;
		this.fillOpacity = 0.15;
		this.lineWidth = 0;
		this.zIndex = 0;
		this.enableMouseTracking = false;
		this.type = 'arearange';
	}
}

