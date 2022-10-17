let bool = 0;

 

///////////////////////////////////////////////////////////////

 

const mysql = require('mysql');

const connection = mysql.createConnection({

  host : 'localhost',

  user : 'skwoo',

  password : 'qazx9911',

  database : 'wi_sun'

});

 

// connection.connect();

 

// connection.query('SELECT * from park_info', (error, rows, fields) => {

// if (error) throw error;

// console.log('Park_info is: ', rows);

// });

 

// function db_connect(str){

// connection.connect(function(err) {

// if (err) throw err;

// console.log("Connected!");

// var sql = "INSERT IGNORE INTO park_info (car_id, park_location, park_fee) VALUES ("+"'" + str +"'" + "," + "'" + str + "'" + ", '10')";

// if (bool==1){

// connection.query(sql, function (err, result) {

// if (err) throw err;

// console.log("1 record inserted");

// end_connect();

// });

// bool = 0;

// }

// });  

// }

 

connection.connect();{

  console.log("Connected!");

}

 

function db_connect(str){  
  // 1101020013
    var strInt = parseInt(str);
    var park_num = null;
    park_num = (strInt - (strInt%100000)) / 100000;
    // var park_temp = ((strInt % 1000000000) - (strInt % 10000000))/10000000
    // var park_light = ((strInt % 10000000) - (strInt % 100000)) / 100000;
    var park_line = ((strInt % 100000) - (strInt % 10000)) / 10000;
    var park_1st = ((strInt % 10000) - (strInt % 1000)) / 1000;
    var park_2nd = ((strInt % 1000) - (strInt % 100)) / 100;
    var park_3rd = ((strInt % 100) - (strInt % 10)) / 10;
    var park_4th = ((strInt % 10) - (strInt % 1));



    var sql = "INSERT INTO park_info_8 (park_num, park_line, park_1st, park_2nd, park_3rd, park_4th)  VALUES (" + "'" + park_num + "'" + "," + "'" + park_line + "'" + "," + "'" + park_1st + "'" + ","+ "'" + park_2nd + "'" + ","+ "'" + park_3rd + "'" + "," + "'" + park_4th + "') " +
              "ON DUPLICATE KEY UPDATE park_1st = " + "'" + park_1st + "', " + "park_2nd = " + "'" + park_2nd + "', " + "park_3rd = " + "'" + park_3rd + "', " + "park_4th = " + "'" + park_4th + "'";

    if (bool==1){

      connection.query(sql, function (err, result) {

        if (err) throw err;

        console.log("1 record inserted");

        //end_connect();

      });

      bool = 0;

    }

}

 

// function end_connect(){

//   connection.end()

//       console.log("Connect Ended");

// }

 

// connection.connect(function(err) {

// if (err) throw err;

// console.log("Connected!");

//  var sql = "INSERT INTO park_info (car_id, park_location, park_fee) VALUES (" + "'" + msg + "'" + "," + "'" + msg + "'" + ", '10')";

// if (bool==1){

// connection.query(sql, function (err, result) {

// if (err) throw err;

// console.log("1 record inserted");

// });

// bool = 0;

// }

// });

 

//connection.end();

 

var PORT = 61617;

 

var HOST = '::1';

 

var dgram = require("dgram");

 

var server = dgram.createSocket("udp6");

 

server.on("error", function (err) {

  console.log("server error:\n" + err.stack);

  server.close();

});

 

server.on("message", function (msg, rinfo) {

  console.log("server got: " + msg + " from " +

    rinfo.address + ":" + rinfo.port);

  bool = 1;

  db_connect(msg);

});

 

server.on("listening", function () {

  var address = server.address();

  console.log("server listening " +

      address.address + ":" + address.port);

});

 

server.bind(PORT);