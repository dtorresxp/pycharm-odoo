# pycharm-odoo
Odoo is a great open source ERP. This plugin provides Odoo coding assistance in PyCharm.

## Features
* Understands relations between modules, models (inheritance, extension and delegation) to resolve references and provide code completion for:
    * Model members (fields, function,...)
    * Odoo addons imports
    * Depends and data files path in manifest
    * Model name in _inherit, env and comodel argument in relation field declaration
    * Compute, inverse methods in field declarations
    * Field path in api.depends, mapped(), ...
    * XML ID in env.ref(), request.render()
* Quick search and navigate to XML ID in project

## Planned features
* XML and JavaScript coding assistance

## Usage
1. Install this plugin from [Jetbrains Plugins Repository](https://plugins.jetbrains.com/plugin/13499-pycharm-odoo/)
or download and install latest version at [here](https://github.com/trinhanhngoc/pycharm-odoo/releases).
2. Clone [odoo-stubs](https://github.com/trinhanhngoc/odoo-stubs) and attach to your project
![](images/odoo-stubs.png)

## Screenshots
![Model name completion](images/model-name-completion.png)

![Go to model declaration](images/go-to-model-declaration.png)

![Model member completion](images/model-member-completion-1.png)

![Model member completion](images/model-member-completion-2.png)

![Field path completion](images/field-path-completion.png)

![Model member completion](images/model-member-completion-3.png)

## Screencast
https://www.youtube.com/watch?v=SMqepH2A4_4