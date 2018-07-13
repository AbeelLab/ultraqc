# Research into visualisation methods

# Introduction

To visualize our results, we have decided to use a HTML-based report. This allows for excellent compatibility with different devices and operating systems, and separates the data analysis from the data visualization clearly. 

This means that we need to find a way to display charts effectively in HTML, using JavaScript for interactivity. Our options are to create our own graphing library, or to use one of several available ones. To avoid reinventing the wheel, we would like to look into the existing options first to determine if those are suitable for our needs.

# Possible options
We have conducted research into commonly used existing libraries for this purpose. The options we have found are:
- Google Charts
- Charts.js	
- D3.

Each of them have several pros and cons, which will be discussed below.
##### Google Charts
Google Charts is a library made by Google that provides around 30 different pre-made charts, with interactivity built in. It provides many customization options and gives us the ability to create our own charts if needed. This is an excellent choice for both basic and advanced charts. 

However, this library comes with one major drawback. The Terms of Service forbid using the library offline, which means that an internet connection would always be required. This creates a huge dependency on Google and limits what we are able to do with the library. Therefore, this library is not an option for us.

##### Chart.js
Charts.js is a basic library for creating six pre-made chart types. It has limited customization, but makes it quick and easy to set up the charts. Interactivity is also included. For more advanced charts, this may not be a suitable option. 

We will likely use this library for our first version, as we have no requirement for very advanced charts yet. Eventually, we may replace it with a different solution.

##### D3
D3 is an advanced library for creating a huge variety of custom-made charts. Customization options are virtually endless, and it allows for very advanced data visualization methods. Creating simple charts in D3 is more challenging than in one of the other libraries, however, it comes with the benefit of much more powerful advanced charts. 

This is an excellent choice for large-scale data visualization projects, however, the increased complexity may put it out of the scope of our project. Therefore we will not be using this solution unless we will be forced to implement more complex visualizations.
Creating our own
Creating our own library is a possibility, however, this will be a significant amount of work and may very well end up taking up the majority of our project. Although this will ensure that we have a library that’s tailored exactly to our needs, it is unfortunately well out of the scope of the project.

# Conclusion
For the time being, we will go with Charts.js. This is a rather limited option, however, it will save us a significant amount of time over a custom library or D3. If we need to create more complex charts, we may switch to D3, or combine both libraries to save time.

