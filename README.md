# KDHost

## Application plan
This document is a roadmap/plan of functions this app will perform.

## Application functions:
CLI application to communicate with host.

Global parameters:

* `KDHOST_HOST` - host name/adres and port in format `host:port`. Default value: `127.0.0.1:19200`
* `KDHOST_USER` - user used to login to host, default value: "1"
* `KDHOST_PASS` - password for login to host. default "xxx".

Example:

```bash
java -DKDHOST_HOST=127.0.0.1:11111 -jar kdhost.jar list procedure
```

Aliases can make this much better:

```bash
alias p761env="java -DKDHOST_HOST=127.0.0.1:11111 -jar kdhost.jar"
alias pipenv="java -DKDHOST_HOST=127.0.0.1:61012 -jar kdhost.jar"

# and then
pipenv list procedure
p761env get MRPC029.PROC
```

Requirements:

* Java (JRE) 1.8+
* Network connection to host (PIPv0.2+), but P7.5+ is recomended.

## Compatiblity

|Host version|compile|drop |fwkinfo|get  |install|list |mrpc |send |sql  |refresh |test |tsc  |watch | 
|:----------:|:-----:|:---:|:-----:|:---:|:-----:|:---:|:---:|:---:|:---:|:------:|:---:|:---:|:----:|
|PIP v0.2    | ❌    | ❌  | ❌    | ✔    | ❌   | ❌ | ⁉️  | ✔   | ✔  | ✔      | ❌   | ❌  | ❌  |
|P7.5.3      | ✔     | ❌  | ✔     | ✔ 💪 | ❌   | ✔   |  ✔  |✔    |✔   |✔       |✔     |✔    | ✔   |
|P7.6.1      | ✔ ️   | ✔   | ✔     | ✔ 💪 | ✔    | ✔   | ✔   |✔    |✔   |✔       |✔     |✔    |✔    |
|P7.6.2      | ✔ ️   | ✔ ️ | ✔     | ✔ 💪 | ✔    | ✔   | ✔   |✔    |✔   |✔       |✔     |✔    |✔    |

* ✔️ - supported
* ⁉️ - not all options are supported, check command section for details.
* ❌ - not supported
* ❓ - not tested
* 💪 - xxxall version of the command is supported to.

## Commands

### Generic options

* `-v LEVEL` This option changes verbosity of output. The levels in descending order are:
  * SEVERE (highest value)
  * WARNING (default value)
  * INFO
  * CONFIG
  * FINE
  * FINER
  * FINEST (lowest value)
  * ALL - will display ALL messages
* `-h, --help` Show help/usage information and quit program.
* `-V, --version` Print versin information

### compile

will compile in host elements with given names (name should include extension).

### drop

Will drop/remove element form host. Supported from P761.

### fwkinfo

Gets information about PSL/Fwk from Host. Supported requests: classes, keywords,functions,featuers
Available options:

* `--all` Will list all elements that can be listed 

### get

This command can be used to get specific elements from host. It takes list of elements to download as space separated list (name should include extension).

Available options:

* `--force` Override file if it exist localy
  
#### get command example

![kdhost get MRPC121.PROC](/doc/img/get_mrpc121_1.gif)

`kdhost get MRPC121.PROC && cat dataqwik/procedure/MRPC121.PROC`

### getall

Command will download all elements of specific type. TYou need to specify element type (types) you would like to download.

Available options:

* `--all` Will download all element types that can be listed. psl, pslx and DAT files are excluded. This option can be used to perform environment extract.
* `-f, --filer=<filer>`      Get all elements belonging to table filer.
* `-t, --table=<table>`       Get table, column definition for specific table.
* `--force` Override file if it exist localy
  
#### getall command example

* `kdhost getall procedure` - will download all procedure files from host
* `kdhost getall --all` - will make environment extract
* `kdhost getall -t DEP` - will download TBL and COL elements
* `kdhost getall -f DEP` - will download TBL, COL, IDX, JFD, TRG and RegordTABLE.psl

### install (#experimental, #p761+)

This command expects valid path to *unpacked* SP or FP. It will read content of it and use online services to 'instal' elements.

⚠ This is command just simulates instalation with bulk send and compile command in proper order. But keep in mind that some actions like index regeneration, screen, report compilation are still missing. Some elements like big filers will not compile due to time outs. **Use this command just for simple FP/SP that don't have complicated elements and compilation will be fast**.

### list

List elements from host.

Available options:

* `--list-listable` Provide list of element types that can be listed
* `--list-types` Provide list of all supported element types
* `-n, --as-names` Print element names instead element file names. Skips extensions.
* `-t, --from-table=<table>`  Get subelement for specific table.
* `--all` Will list all elements that can be listed

#### list command example

[![asciicast](https://asciinema.org/a/212519.svg)](https://asciinema.org/a/212519)
`kdhost list procedure` will list all procedures from host, one per line.

[![asciicast](https://asciinema.org/a/212521.svg)](https://asciinema.org/a/212521)
`kdhost list triggers -t DEP` Will list triggers for DEP table.

### mrpc

This command can be used to call any MRPC from host. User his class should have authorization to call this MRPC.

Available options:

* `-mv,--mrpc-version` MRPC version to use, default: "1"
* `-dp, --describe-parameters` - Get list of MRPC parameters, Not supported in PIP.

Parameters:

* `MRPCID` - MRPCID, as defined on host. This is expected as first parameter.
* `PARAMETERS` - MRPC parameters, excluding version. From second parameter.

#### mprc command example

![kdhost mrpc select](/doc/img/mrpc_example.gif)
`kdhost mrpc  121 INITOBJ "" "" "" Procedure MRPC121`

### send

Can send element(s) to host. Will take element form path and send it to host. '*' can be used to send multiple elements from on folder. This command is not recursive.

### sql

Will execute SQL query on host. No validation and checks are made.
Available options:

* `-s, --separator` Separator that is used in column separation, default value = `|`

#### sql command example

[![asciicast](https://asciinema.org/a/212522.svg)](https://asciinema.org/a/212522)

`kdhost sql "select fid,des from dbtbl1" -s=" | "`

### refresh

Will redownload elements existing in local direcories.

#### refresh command example

`kdhost refresh dataqwik/pocedure` - Will rrefresh all procedures that are on local drive. Will not download new ones from host

### test

Will test compile element on host.

### tsc

Will execute following commands in chain: `test` `send` `compile`. Error from any of them will stop further execution.

### watch

Will start watching specific directory and execute `tsc` or `drop` command on changes. This command is recursive.

## Tips and Tricks

1. Download elements with specific patern in name:

    * `kdhost get -v ALL $(kdhost list procedure | grep ^DE | awk '{print}' ORS=' ')` - will get all procedures which name starts with `DE` (^DE).
    * `kdhost get -v ALL $(kdhost sql "select FID from DBTBL1" | awk '{print}' ORS='.DAT ' )` - will download all TABELS (without BLOB/CLOB/Memo) columns form host - data extract. Using of this is not a good idea.

2. Display content of any file in env.
    * `kdhost mrpc 121 RETOBJ ../errorlog/2020202020202020.log` - Will display error log file on screen. This can be redirected to file. Just remember to skip "RESPONSE: " string from begining. This will remove file after this command finishes. Be awared.


## Bash and Zsh completion

Installation

1. Source all completion scripts in your .bash_profile

```bash
cd $YOUR_APP_HOME/bin
for f in $(find . -name "*_completion"); do line=". $(pwd)/$f"; grep "$line" ~/.bash_profile || echo "$line" >> ~/.bash_profile; done
```

Alternatively, if you have [bash-completion](https://github.com/scop/bash-completion) installed:
     Place this file in a `bash-completion.d` folder:

* /etc/bash-completion.d
* /usr/local/etc/bash-completion.d
* ~/bash-completion.d

2. Open a new bash console, and type `kdhost [TAB][TAB]`

## Note

All exmples made with PIP v0.2 - If you are reading this - then you know what it is.