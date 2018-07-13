/**
 * Method to add a quality bar.
 *
 * @param value The value
 * @param row The row
 * @param index The index
 * @param field The field
 * @returns {string} The bar
 */
function qualityCountFormatter(value, row, index, field) {
	function createBarSection(classes, amount, percentage, isSelected) {
		var content = "";
		
		// show appropriate amount of information on bar to prevent overflowing text
		if (percentage > 12) content = amount + ' (' + Math.round(percentage) + '%)';
		else if (percentage > 6) content = Math.round(percentage) + '%'; 
		
		if (isSelected) { classes += " selected"; }
		
		return amount == 0 ? '' : 
			'<div row-index="' + index + '" role="progressbar" style="width: ' + percentage + '%"'
				+ ' class="clickable progress-bar bg-' + classes + ' quality_dist"'
				+ ' aria-valuenow="' + percentage + '" aria-valuemin="0" aria-valuemax="100"'
				+ ' title="' +  amount + ' (' + Math.round(percentage) + '%)' + '">'
				+ content
			+ '</div>'
	}
	
	if (!row.god) { row.god = row.Amount - row.avg - row.bad; }
	
	// calculate the percentages
	var avg = 100 * row.avg / row.Amount;
	var bad = 100 * row.bad / row.Amount;
	var god = 100 - avg - bad;
	
	// generate the bars
	return '<div class="progress" style="height: 30px; width: 450px;">'
		+ createBarSection("danger", row.bad, bad, row.selectedQuality == "bad")
		+ createBarSection("warning", row.avg, avg, row.selectedQuality == "avg")
		+ createBarSection("success", row.god, god, row.selectedQuality == "god")
	+'</div>'
}


/**
 * Method to show the selected samples.
 *
 * @param value The value
 * @returns {string} The html for badges
 */
function selectedSampleFormatter(value) {
	function qualitySelectionBadge(type, amount) {
		if (!amount) { return ""; }
		return "<span class='badge badge-" + type + "'>" + amount + " selected</span>"
	}
	
	if (!value) return "";
	
	var content = [];
	
	content.push(qualitySelectionBadge("success", value.god));
	content.push(qualitySelectionBadge("warning", value.avg));
	content.push(qualitySelectionBadge("danger", value.bad));

	return content.filter(el => el != "").join("<br/>");
}


/**
 * Method to add a percentage symbol.
 *
 * @param value The value
 * @returns {string} The value with a percentage sign
 */
function percentageFormatter(value) {
	return value + "%";
}

/**
 * Method to add metric badges for average and bad metrics.
 *
 * @param value The value
 * @returns {*} The badge
 */
function metricFormatterAvg(value) { return metricFormatter("warning", value); }
function metricFormatterBad(value) { return metricFormatter("danger", value); }

/**
 * Method to add metric badges for a given type.
 *
 * @param badgeType The type of the badge
 * @param value The value
 * @returns {*} The badge
 */
function metricFormatter(badgeType, value) {
	var metrics = value.split(";").clean();

	return metrics.map(el => "<span title='" + config.metrics[el].tooltip + "' badge-metric='" 
		+ el + "' class='clickable badge badge-" + badgeType + "'>"
		+ config.metrics[el].display + " </span>").join("<br/>");
}