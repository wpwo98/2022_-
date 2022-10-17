const mysql      = require('mysql');
const connection = mysql.createConnection({
  host     : 'localhost',
  user     : 'skwoo',
  password : 'qazx9911',
  database : 'wi_sun'
});

// connection.connect();

// connection.query('SELECT * from park_info', (error, rows, fields) => {
//   if (error) throw error;
//   console.log('Park_info is: ', rows);
// });

connection.connect(function(err) {
  if (err) throw err;
  console.log("Connected!");
  var sql = "INSERT INTO park_info (car_id, park_location, park_fee) VALUES ('a', 'b', '10')";
  connection.query(sql, function (err, result) {
    if (err) throw err;
    console.log("1 record inserted");
    connection.end();
  });

});

