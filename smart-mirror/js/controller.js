(function(angular) {
    'use strict';

    function MirrorCtrl(
            AnnyangService,
            GeolocationService,
            WeatherService,
            $scope, $timeout, $interval, $sce) {
    	
        var _this = this;
        var command = COMMANDS.ko;
        var functionService = FUNCTIONSERVICE;
        var DEFAULT_COMMAND_TEXT = command.default;
        var PHOTO_INDEX=0;
        var VIDEO_INDEX=0;
        $scope.listening = false;
        $scope.complement = command.hi;
        $scope.debug = false;
        $scope.focus = "default";
        $scope.greetingHidden = "true";
        $scope.user = {};
        $scope.interimResult = DEFAULT_COMMAND_TEXT;
        
        /** Smart Mirror IP */
        var os = require('os');
        var networkInterfaces = os.networkInterfaces();
        $scope.ipAddress = networkInterfaces.wlan0[0].address;
        
        // Update the time
        function updateTime(){
            $scope.date = new Date();
        }

        // Reset the command text
        var restCommand = function(){
          $scope.interimResult = DEFAULT_COMMAND_TEXT;
        }

        _this.init = function() {
        	$scope.map = MapService.generateMap("Seoul,Korea");
            var tick = $interval(updateTime, 1000); // 1초 마다
            updateTime();

            /** 현재 장소를 가져오며, 날씨 정보를 가져온다. */
            var refreshMirrorData = function() {
                //Get our location and then get the weather for our location
                GeolocationService.getLocation({enableHighAccuracy: true}).then(function(geoposition){
                    console.log("Geoposition", geoposition);
                    WeatherService.init(geoposition).then(function() {
                        $scope.currentForcast = WeatherService.currentForcast();
                        $scope.weeklyForcast = WeatherService.weeklyForcast();
                        $scope.hourlyForcast = WeatherService.hourlyForcast();
                        console.log("Current", $scope.currentForcast);
                        console.log("Weekly", $scope.weeklyForcast);
                        console.log("Hourly", $scope.hourlyForcast);
                    });
                }, function(error){
                    console.log(error);
                });

                /** config.js의 greeting 배열(인사말의 정보)를 랜덤으로 가져온다 */
                $scope.greeting = config.greeting[Math.floor(Math.random() * config.greeting.length)];
            };

            refreshMirrorData();
            $interval(refreshMirrorData, 3600000);

            /* Default뷰는 홈 화면*/
            var defaultView = function() {
            	functionService.defaultHome($scope);
            }

            // 미러는 누구니
            AnnyangService.addCommand(command.whois,function() {
            	functionService.whoIsSmartMirror($scope);
            });
            
            // 사용가능한 명령을 보여준다.
            AnnyangService.addCommand(command.whatcanisay, function() {
               functionService.whatCanISay($scope);
            });

            // 홈화면으로
            AnnyangService.addCommand(command.home, defaultView);

            // 미러의 화면을 끈다.
            AnnyangService.addCommand(command.sleep, function() {
            	functionService.goSleep($scope);
            });

            // 미러의 화면을 켠다.
            AnnyangService.addCommand(command.wake, function() {
            	functionService.wake($scope);
            });

            // 디버그의 정보를 보여준다.
            AnnyangService.addCommand(command.debug, function() {
                console.debug("Boop Boop. Showing debug info...");
                $scope.debug = true;
            });
            
/*
            //안드로이드에서 보낸 SST 명령어를 미러와 동작하게 하는 부분
            var sender = require('remote').getGlobal('sender');
     	    sender.on('android',function(android){
     	    	$scope.interimResult = android.command; // 미러의 음성인식된 문구에 보여짐
	    		console.log("Android Command :: "+android.command);
	    		var androidCommand = android.command+"";
	    		
    			if(androidCommand === command.sleep) { functionService.goSleep($scope);}
    			else if(androidCommand === command.whois) { functionService.whoIsSmartMirror($scope); }
    			else if(androidCommand === command.home) { functionService.defaultHome($scope); }  
    			else if(androidCommand === command.wake) { functionService.wake($scope); }
    			else if(androidCommand === command.whatcanisay) { functionService.whatCanISay($scope); }
    			else if(androidCommand === command.map) { functionService.map($scope,GeolocationService,MapService); }
    			else if(androidCommand === command.news) { functionService.news($scope); }
    			else if(androidCommand === command.photo) { functionService.photo(); }
    			else if(androidCommand === command.video) { functionService.video(); }
    			else if(androidCommand === command.lighton) { functionService.lightOn();}
    			else if(androidCommand === command.lightoff) { functionService.lightOff();}
    			
    			
    			// Map Service ***의 위치 보여줘 
    			var locationExist = androidCommand.indexOf("위치");
	    		if(locationExist != -1) {
	    			var locationValue = androidCommand.split("위치");
	    			console.log(locationValue[0]);
	    			functionService.location(locationValue[0],$scope,GeolocationService,MapService);
	    		}
	    		
	    		// Youtube *** 동영상 보여줘 
	    		var youtubeExist = androidCommand.indexOf("동영상");
	    		if(youtubeExist != -1) {
	    			if(androidCommand === "동영상 정지") {
	    				functionService.stopYoutube($scope);
	    			}else {
		    			var youtubeValue = androidCommand.split("동영상");
		    			console.log(youtubeValue[0]);
		    			functionService.playYoutube(youtubeValue[0],$scope,$sce,YoutubeService);
	    			}
     	    	}
	    		
	    		// 지하철 **역 *호선 *행성 
	    		var subwayExist = androidCommand.indexOf("역");
	    		if(subwayExist != -1) {
	    			// OO역 OO호선 상(하)행선
	    			var temp1 = androidCommand.split("역");
	    			var temp2 = temp1[1].split("호선");
	    			
	    			var subwayStation = temp1[0];
	    			var subwayLineNumber = temp2[0].trim();
	    			var subwayUpDown = temp2[1].trim();
	    			console.log(subwayStation+"역"+subwayLineNumber+"호선"+subwayUpDown);
	    			functionService.subway(subwayStation,subwayLineNumber,subwayUpDown,$scope,SubwayService);
	    		}	    		
    	    });
*/
            var resetCommandTimeout;
            //Track when the Annyang is listening to us
            AnnyangService.start(function(listening){
                $scope.listening = listening;
            }, function(interimResult){
                $scope.interimResult = interimResult;
                $timeout.cancel(resetCommandTimeout);
            }, function(result){
                $scope.interimResult = result[0];
                resetCommandTimeout = $timeout(restCommand, 5000);
            });
            
            $scope.interimResult = DEFAULT_COMMAND_TEXT; // 미러의 음성인식된 문구에 보여짐
        };
        _this.init();
    }
    angular.module('SmartMirror')
        .controller('MirrorCtrl', MirrorCtrl);
}(window.angular));