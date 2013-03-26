Ext.define('DnsManagement.appconfig.AppConfig',

	statics:
		PRODUCTION_ENV: 'production'

	config:
		environment: null

		production:
			endpoints:
				records:
					url: 'api/records'
					root: 'http://localhost:9000/'

	constructor: ( cfg ) ->
		@setEnvironment(DnsManagement.appconfig.AppConfig.PRODUCTION_ENV)

	getEndpoint: ( endpointName ) ->
		environmentConfig = @[ @getEnvironment() ]
		endpoints = environmentConfig.endpoints
		defaults = environmentConfig.defaults

		if endpoints?[ endpointName ]
			result =
				url: endpoints[endpointName].url
				root: endpoints[endpointName]?.root ? defaults.dataRoot


)