<?php 

    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');
        

    $stmt = $con->prepare('select * from park_info_8');
    $stmt->execute();

    if ($stmt->rowCount() > 0)
    {

        $data = array(); 

        while($row=$stmt->fetch(PDO::FETCH_ASSOC))
        {
            extract($row);
    
            array_push($data, 
                array(
                'park_num'=>$park_num,
                'park_temp'=>$park_temp,
                'park_light'=>$park_light,
                'park_line'=>$park_line,
                'park_1st'=>$park_1st,
                'park_2nd'=>$park_2nd,
                'park_3rd'=>$park_3rd,
                'park_4th'=>$park_4th,
            ));
        }
        

        header('Content-Type: application/json; charset=utf8');
        $json = json_encode(array('skwoo'=>$data), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
        echo $json;
    }

?>