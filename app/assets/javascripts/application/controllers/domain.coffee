class Custom.DomainController extends Application.Controller

	el: '#contents'
	
	elements:
		'#button': 'button'
		
	events:
		'mouseenter tr': 'toggle'
		'mouseleave tr': 'toggle'
		'click #button': 'introduce'
		
	init: ->
		console.log 'init ...'
		@domains = new Custom.Domains()
		@domains.fetch()
		console.log('init: domains', @domains)
		@render()
		
	introduce: ->
		@domains.each (domain) ->
			alert domain.introduce()
			
	toggle: (event, target) ->
		target.toggleClass 'active'
		
	render: (domains) ->
		table = Mooml.render 'items-list',
		  caption: 'Domains List'
		  header: [['Id', 'Name', 'Master', 'Last Check', 'Domain Type', 'Notified Serial', 'Account']]
		  items: @domains.map (domain) ->
		  	Object.values Object.subset domain.toJSON(),
		  	   [['id', 'name', 'master', 'lastCheck', 'domainType', 'notifiedSerial', 'account']]
		  	console.log("domains: to json", domain.toJSON())
		console.log('controller :: render domains:', table)
		@el.adopt table
		@refresh_elements()