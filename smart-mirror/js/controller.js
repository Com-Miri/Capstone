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

            /** GPS 정보를 가져온다 */
            GeolocationService.getLocation({enableHighAccuracy: true}).then(function(geoposition){
                console.log("Geoposition", geoposition);
                $scope.map = MapService.generateMap(geoposition.coords.latitude+','+geoposition.coords.longitude);
            });
            restCommand();

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
