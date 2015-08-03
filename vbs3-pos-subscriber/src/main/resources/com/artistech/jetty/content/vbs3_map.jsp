<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
com.artistech.vbs3.JettyBean jettyBean = new com.artistech.vbs3.JettyBean();
%>
<html>
    <head>
        <title>VBS3 - Map Test (D3JS)</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="icon" 
              type="image/png" 
              href="favicon.png" />
        <link rel="stylesheet" href="style.css" type="text/css">
        <script type="text/javascript" src="./js/d3-3.5.6-min.js"></script>
        <script type="text/javascript" src="./js/queue-1.0.7.js"></script>
        <script type="text/javascript" src="./js/topojson-1.6.19.js"></script>
        <script type="text/javascript" src="./js/d3-tip-0.6.7.js"></script>

        <script type="text/javascript" src="./js/Long-2.2.5-min.js"></script>
        <script type="text/javascript" src="./js/ByteBufferAB-4.0.0-min.js"></script>
        <script type="text/javascript" src="./js/ProtoBuf-4.0.0-min.js"></script>
        <script type="text/javascript">
            if (typeof dcodeIO === 'undefined' || !dcodeIO.ProtoBuf) {
                throw(new Error("ProtoBuf.js is not present. Please see www/index.html for manual setup instructions."));
            }
            // Initialize ProtoBuf.js
            var ProtoBuf = dcodeIO.ProtoBuf;
            var Position = ProtoBuf.loadProtoFile("./Vbs3GetPos.proto").build("VBS3.Position");
            var ws = null;

            function webSocketTest()
            {
                if (ws === null) {
                    // Let us open a web socket
                    var ws_uri = "<%= jettyBean.getServer() %>";
                    ws = new WebSocket("ws://" + ws_uri + "/getpos");
                    ws.binaryType = "arraybuffer";

                    ws.onopen = function ()
                    {
                        connected = true;
                        // Web Socket is connected, send data using send()
                        console.log("Connected...");
                        //ws.send("Message to send");
                        //alert("Message is sent...");
                    };

                    ws.onmessage = function (evt)
                    {
                        try {
                            var msg = Position.decode(evt.data);
                            msg.id = msg.id.replace(/\"/g, "");
                            msg.id = "#player_" + msg.id;
                            createPlayer(msg);
                            movePlayer(msg);
                            rotatePlayer(msg);
                            updateTip(msg);
                        } catch (err) {
                            console.log(err);
                        }
                    };

                    ws.onclose = function ()
                    {
                        ws = null;
                        console.log("Connection is closed...");
                        // websocket is closed.
                        //alert("Connection is closed..."); 
                    };
                }
            }
            function stopWebSocketTest() {
                if (ws !== null) {
                    ws.close();
                    ws = null;
                } else {
                    console.log("Connection already closed...");
                }
            }
        </script>
        <style>
            .g-graphic {
                position: relative;
            }

            .g-graphic svg {
                border-top: solid 1px #ccc;
                font-family: Arial;
            }

            .g-background {
                fill: #e0e9ef;
            }

            .g-trail,
            .g-track {
                fill: none;
                stroke: #000;
                stroke-linejoin: round;
                stroke-linecap: round;
            }

            .g-track {
                stroke-opacity: .2;
            }

            .g-trail {
                stroke-width: 1.5px;
            }

            .g-boat circle {
                stroke: #000;
                stroke-opacity: .1;
                stroke-width: 3px;
            }

            .g-boat text {
                text-anchor: middle;
                fill: white;
                stroke: none;
                font-family: Arial;
                font-size: 9px;
            }

            .g-compass circle{
                fill: white;
                fill-opacity: 0.5;
                stroke: none;
                stroke-opacity: 0.2;
            }

            .g-compass line {
                stroke: grey;
            }


            .d3-tip {
                line-height: 1;
                font-weight: bold;
                padding: 12px;
                background: rgba(0, 0, 0, 0.8);
                color: #fff;
                border-radius: 2px;
            }

            /* Creates a small triangle extender for the tooltip */
            .d3-tip:after {
                box-sizing: border-box;
                display: inline;
                font-size: 10px;
                width: 100%;
                line-height: 1;
                color: rgba(0, 0, 0, 0.8);
                content: "\25BC";
                position: absolute;
                text-align: center;
            }

            /* Style northward tooltips differently */
            .d3-tip.n:after {
                margin: -1px 0 0 0;
                top: 100%;
                left: 0;
            }
        </style>
    </head>
    <body>
        <div id="map">
            <h1>Example of using WebSockets to receive updates about position.</h1>
            <ul>
                <li><a href="javascript:webSocketTest()">Start Receiving Data</a></li>
                <li><a href="javascript:stopWebSocketTest()">Stop Receiving Data</a></li>
            </ul>
            <div class="g-graphic">
            </div>
            <script>
                var created_players = {};
                var width = 750,
                        height = 750;

                function projection(msg) {
                    var w = msg.worldCenterX * 2;
                    var h = msg.worldCenterY * 2;
                    var wRatio = width / w;
                    var hRatio = height / h;
                    var x = wRatio * msg.x;
                    var y = height - (hRatio * msg.y);
                    return [x, y];
                }

                var path = d3.geo.path()
                        .pointRadius(3.5);

                var svg = d3.select(".g-graphic").append("svg")
                        .attr("width", width)
                        .attr("height", height);

                svg.append("rect")
                        .attr("class", "g-background")
                        .attr("width", width)
                        .attr("height", height + 1);

                function movePlayer(msg) {
                    created_players[msg.id].boat.attr("transform", "translate(" + projection(msg) + ")");
                    return this;
                }

                function rotatePlayer(msg) {
                    created_players[msg.id].compass.attr("transform", "rotate(" + msg.dir + ")");
                    return this;
                }

                function updateTip(msg) {
                    var tmp_id = msg.id;//.replace(/#/g, "");
                    tmp_id = tmp_id.replace(/#/g, "");
                    var elem = document.getElementById("tip-id-" + tmp_id);//.innerHTML = msg.dir;
                    if (elem !== null) {
                        var htm = "<strong>ID: </strong> <span style='color:red'>" + msg.id + "</span><br />";
                        htm += "<strong>X: </strong> <span style='color:red'>" + msg.x + "</span><br />";
                        htm += "<strong>Y: </strong> <span style='color:red'>" + msg.y + "</span><br />";
                        htm += "<strong>Dir: </strong> <span style='color:red'>" + msg.dir + "</span><br />";
                        elem.innerHTML = htm;
                    }
                    return this;
                }

                function createPlayer(msg) {
                    if (msg.id in created_players) {
                        return this;
                    }
                    var boats = [
                        {type: "LineString", id: msg.id, data: msg}
                    ];

                    var tip = d3.tip()
                            .attr('class', 'd3-tip')
                            .offset([-10, 0])
                            .html(function (d) {
                                var tmp_id = msg.id;//.replace(/#/g, "");
                                tmp_id = tmp_id.replace(/#/g, "");
                                return "<div style=\"width: 250px; height: 50px\" id=\"tip-id-" + tmp_id + "\"></div>";
                            });

                    var track = svg.selectAll(".g-track")
                            .data(boats)
                            .enter().append("path")
                            .attr("class", function (d) {
                                return "g-track g-track-" + d.id;
                            });

                    var trail = svg.selectAll(".g-trail")
                            .data(boats)
                            .enter().append("path")
                            .attr("class", function (d) {
                                return "g-trail g-trail-" + d.id;
                            });

                    var boat = svg.selectAll(".g-boat")
                            .data(boats)
                            .enter().append("g")
                            .attr("id", function (d) {
                                return "g-boat-" + d.id;
                            })
                            .attr("class", function (d) {
                                return "g-boat g-boat-" + d.id;
                            })
                            .on('mouseover', tip.show)
                            .on('mouseout', tip.hide);

                    var compass = boat.append("g")
                            .attr("class", "g-compass")
                            .attr("id", function (d) {
                                return "g-compass-" + d.id;
                            });

                    svg.call(tip);

                    compass.append("circle")
                            .attr("r", 10);

                    compass.append("line")
                            .attr("x1", 0)
                            .attr("x2", 0)
                            .attr("y1", 10)
                            .attr("y2", -10);

                    compass.append("line")
                            .attr("x1", 0)
                            .attr("x2", -4)
                            .attr("y1", -10)
                            .attr("y2", -6);

                    compass.append("line")
                            .attr("x1", 0)
                            .attr("x2", 4)
                            .attr("y1", -10)
                            .attr("y2", -6);

                    console.log("created player: " + msg.id);
                    created_players[msg.id] = new Object();
                    created_players[msg.id].boat = boat;
                    created_players[msg.id].track = track;
                    created_players[msg.id].trail = trail;
                    created_players[msg.id].compass = compass;
                    created_players[msg.id].tip = tip;
                    return this;
                }
            </script>
        </div>
    </body>
</html>
