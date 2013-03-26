Ext.Loader.setConfig
	enabled: true
	paths:
    	"DnsManagement": "/assets/javascripts/app"
		"Ext.ux": "lib/ux/"


Ext.application
  autoCreateViewport: true
  name: "DnsManagement"
  appFolder: "assets/javascripts/app"

Ext.require "DnsManagement.store.RecordDataJson"

Ext.onReady ->
  Deft.Injector.configure
    recordDataJson: "DnsManagement.store.RecordDataJson"
    