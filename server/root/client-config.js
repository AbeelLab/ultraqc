const colours = {
	"GOOD": '#D3FFD3',
	"AVERAGE": '#FFEFD3',
	"BAD": '#FFD3D3' 
}

const fastqcGraphs = [
	"per_sequence_gc_content",
	"avg_gc_content",
	"per_base_n_content",
	"per_base_sequence_quality",
	"per_sequence_quality_scores",
	"sequence_length_distribution"
];

var config = {
	metrics: {
		"per_sequence_gc_content": { 
			display: "Normalized GC content",
			tooltip: "Normalized GC content. A distribution graph showing the GC content of each sample, normalized around the origin."
		},
		"avg_gc_content": {
			display: "Average GC content",
			tooltip: "Average GC content. Show the mean of the GC content for each sample (e.g. the mean of the non-normalized GC content graph.)",
			slider: {min: 0, max: 1},
			type: "bar"
		},
		"per_base_n_content": { 
			display: "N-Content",
			tooltip: "Per Base N Content. The relative amount of N calls per base pair."
		},
		"per_base_sequence_quality": {
			display: "Sequence Quality",
			tooltip: "Per Base Sequence Quality. The Phred score per base pair."
		},
		"per_sequence_quality_scores": { 
			display: "Quality Scores",
			tooltip: "Per Sequence Quality Score. The absolute amount of reads with a certain Phred score."
		},
		"sequence_length_distribution": { 
			display: "Relative Length Distribution",
			tooltip: "Sequence Length Distribution. The absolute amount of reads normalized between 0 and 100."
		}	
	},
	charts: {
		"per_sequence_gc_content": {
			"xAxis": { "title": { "text": "Normalized GC percentage" } },
			"yAxis": { 
				"title": { "text": "Percentage" }, 
				"min": 0
			}
		},
		"avg_gc_content": {
			"xAxis": { "title": { "text": "Sample name" } },
			"yAxis": { "title": { "text": "Mean gc content" } }
		},
		"per_base_n_content": {
			"xAxis": { "title": { "text": "Position in read (bp)" } },
			"yAxis": { 
				"title": { "text": "Percentage N-count" },
				"plotBands": [
					{ "color": colours.GOOD, "from": 0, "to": 5 }, 
					{ "color": colours.AVERAGE, "from": 5, "to": 20},
					{ "color": colours.BAD, "from": 20, "to": 100} 
				],
				"min": 0
			}
		},
		"per_base_sequence_quality": {
			"xAxis": {"title": { "text": "Position in read (bp)" },},
			"yAxis": { 
				"title": { "text": "Mean Sequence Quality (Phred Score)" }, 
				"plotBands": [
					{ "color": colours.GOOD, "from": 25, "to": 45 }, 
					{ "color": colours.AVERAGE, "from": 20, "to": 25},
					{ "color": colours.BAD, "from": 0, "to": 20}
				],
			"min": 0
			}
		},
		"sequence_length_distribution": {
			"xAxis": { "title": { "text": "Relative Sequence Length" } },
			"yAxis": { 
				"title": { "text": "Read Count" }, 
				"min": 0
			}
		},
		"per_sequence_quality_scores": {
			"xAxis": { 
				"title": { "text": "Mean Sequence Quality (Phred Score)" },
				"plotBands": [
					{ "color": colours.GOOD, "from": 25, "to": 45 },
					{ "color": colours.AVERAGE, "from": 20, "to": 25},
					{ "color": colours.BAD, "from": 0, "to": 20} 
				]		
			},
			"yAxis": { 
				"title": { "text": "Count" }, 
				"min": 0
			}
		}
	}
}
