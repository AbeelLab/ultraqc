var assert = require('assert');
const fs = require('fs');

const config_path = "./tests/resources/config.json";
const sample_path = "./tests/resources/sampleData.json";
const sample_query_path = "./tests/resources/sampleQueryData.json";


const supportedMetrics = [
    "test_quality"
];

// read config file
const config = JSON.parse(fs.readFileSync(config_path, "utf8").toString());
const sample_data = JSON.parse(fs.readFileSync(sample_path, "utf8").toString());
const sample_query_data = JSON.parse(fs.readFileSync(sample_query_path, "utf8").toString());

const util = require("../modules/databaseUtil.js")(config)

describe('buildSelector', function() {
  it('It should return a valid quertPart', function() {
    assert.equal(util.buildSelector("BAD"), "((`test_quality` < 0.3))");
  });  
});

describe('inRange', function() {
  it('It should be in range', function() {
    assert.equal(util.inRange([0, 1], 0.5), true);
  });
  
   it('It should not be in range', function() {
    assert.equal(util.inRange([0, 1], 2), false);
  });
});

describe('getQuality', function() {
  it('It should return GOD quality', function() {
    assert.equal(util.getQuality(0.8, "test_quality", config), "GOD");
  });
  
  it('It should return AVG quality', function() {
    assert.equal(util.getQuality(0.5, "test_quality", config), "AVG");
  });

  it('It should return BAD quality', function() {
    assert.equal(util.getQuality(0.2, "test_quality", config), "BAD");
  });
  
});

describe('ensureCallback', function() {
  it('It should return a valid callback', function() {
    assert.equal(util.ensureCallback(function(stat, msg) {return msg;}, 200, "hello", "error", config), "hello");
  });  
});

describe('merge', function() {
  it('It should correctly merge an array', function() {
    assert.equal(util.merge(sample_data.samples, el => el.metrics["test_quality"].data).length, 5000);
  });  
});

describe('countQuality', function() {
  it('Count per sample the qualities', function() {
    assert.equal(util.countQuality(sample_query_data.data, "test_quality").BAD, 4);
  });  
});

describe('groupBy', function() {
  it('group data', function() {
    assert.equal(util.groupBy(['one', 'two', 'three'], "length")["3"].length, 2);
  });  
});

describe('buildQuery', function() {
  it('Building the query should be correct', function() {
    assert.equal(util.buildQuery("", [{date: 2018, spec: "abc"}, {date: 2017, spec: "def"}, {date: 2016}]).params.length, 3);
  });  
});

describe('buildQueryListOfMetrics', function() {
  it('Creating the query part to list which metrics belong to the give quality should be correct', function() {
    assert.equal(util.buildQueryListOfMetrics("GOD"), "CONCAT(IF((`test_quality` > 0.7), CONCAT('test_quality', ';'), ''))");
  });  
});

describe('buildQueryListOfMetrics', function() {
  it('It should create a valid range of metrics', function() {
    assert.equal(util.buildQueryParts("GOD")[0][0], 'test_quality');
  });  
});

describe('buildQueryPartFromRange', function() {
  it('It should build a single query part', function() {
	var func = util.buildQueryPartFromRange("GOD")
    assert.equal(typeof func, "function");
  });  
});



