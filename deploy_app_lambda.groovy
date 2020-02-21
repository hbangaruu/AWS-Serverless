node ('linux') { 
	properties([
		parameters([
			string(defaultValue: 'Notification', description: 'DTI Gentium Service Name', name: 'serviceName', trim: false),
			string(defaultValue: 'LoadNotifications', description: 'Lambda Func Name', name: 'lambdafuncname', trim: false),
			string(defaultValue: 'Services.Notification.Load', description: 'artifact filter', name: 'filter', trim: false),
		    [$class: 'ChoiceParameter', 
			choiceType: 'PT_SINGLE_SELECT', 
			description: '', 
			filterLength: 1, 
			filterable: false, 
			name: 'projectName', 
			randomName: 'choice-parameter-6277881166989', 
			script: [$class: 'GroovyScript', 
			fallbackScript: [classpath: [], 
			sandbox: false, 
			script: 'return "Script Error"'], 
			script: [classpath: [], 
			sandbox: true, 
			script: '''return [\'DTI.Gentium.Services.Notification.Orchestrator\',
			\'DTI.Gentium.Services.PushNotification\',
			\'DTI.Gentium.Services.PendingNotifications\',
			\'DTI.Gentium.Services.ConnectionManager\',
			\'DTI.Gentium.Services.Notification\',
			\'DTI.Gentium.Services.Notification.Load\',
			\'DTI.Gentium.Services.Notification.Shaman\',
			\'DTI.Gentium.Services.Notification.StatusUpdate\']'''
			]
			]
			], 
			[$class: 'CascadeChoiceParameter', 
			choiceType: 'PT_SINGLE_SELECT', 
			description: '', 
			filterLength: 1, 
			filterable: false, 
			name: 'artifact_zip_name', 
			randomName: 'choice-parameter-6277917647000', 
			referencedParameters: 'projectName,filter', 
			script: [$class: 'GroovyScript', 
			fallbackScript: [classpath: [], 
			sandbox: false, 
			script: 'return "Script Error"'],
			script: [classpath: [], 
			sandbox: false, 
			script: '
			import jenkins.model.*
			import hudson.model.*
			import jenkins.*
			import hudson.*
			import java.lang.*
			import groovy.json.JsonSlurper
				def repo = "xyz"
				def zipfilter = filter
				def maxcount = 10
				def user = ""
				def pass = ""
				// Pull username from Jenkins credential store
				def creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.Credentials.class,Jenkins.instance,null,null);
				for (c in creds) {
					if (c.id == "afapi") {
						user = c.username
						pass = c.password
					}
				}
			
				// Now access API to retrieve artifact list
				try {
					List<String> artifacts = new ArrayList<String>()
					def urlStr = "${artifactory}"+projectName+"/?list&deep=1&listFolders=0&mdTimestamps=1&includeRootPath=0"
					def connection = new URL(urlStr).openConnection() as HttpURLConnection
					// set some headers
					connection.setRequestProperty( \'Accept\', \'application/json\' )
					def userCredentials = user + ":" + pass
					def basicAuth = "Basic " + userCredentials.bytes.encodeBase64().toString()
					connection.setRequestProperty(\'Authorization\', basicAuth)
					connection.setRequestProperty( \'Accept\', \'application/json\' )
			
					// Only process if response is 200 (this will also auto-retrieve body)
					if (connection.responseCode == 200) {
						// Parse JSON
						def jsonResp = new JsonSlurper().parseText(connection.inputStream.text)
						// Sort JSON - note that we are using lastModified string reverse
						// sorting. ISO-8869-1 dates are string-sortable
						def jsonSorted = jsonResp.files.sort { a,b -> b.lastModified <=> a.lastModified }
						def count = 0
						for (item in jsonSorted) {
							if (item.uri.contains(zipfilter)){
								artifacts.add(item.uri.drop(1))
							}
							count++
							if (count >= maxcount) break  
						}
					}
					return artifacts
			
				} catch (Exception e) {
				print "There was a problem getting a list of artifacts ${e}"
				}
			''', 
			multiSelectDelimiter: ',', 
			name: 'artifact_name', 
			quoteValue: false, 
			saveJSONParameterToFile: false, 
			type: 'PT_SINGLE_SELECT', 
			visibleItemCount: 10
			']
			]
			]
		])
	])
}
