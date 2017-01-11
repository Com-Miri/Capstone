var FUNCTIONSERVICE = {
	defaultHome : function($scope) {
		console.debug("Ok, going to default view...");
        if(responsiveVoice.voiceSupport()) {
          responsiveVoice.speak("홈으로 이동합니다.","Korean Female");
        }
        $scope.focus = "default";
	},
	whoIsSmartMirror : function($scope) {
		console.log("Who is Smart Mirror");
		if(responsiveVoice.voiceSupport()) {
	          responsiveVoice.speak("저는 음성 인식이 가능한 스마트 미러입니다.","Korean Female");
        }
		$scope.focus = "whoissmartmirror";
	},
	goSleep : function($scope){
		console.debug("Ok, going to sleep...");
        if(responsiveVoice.voiceSupport()) {
          responsiveVoice.speak("자러 갈게요. 다음에 봐요!","Korean Female");
        }
        $scope.focus = "sleep";
	},
	wake : function($scope) {
		console.debug("Wake up...");
		if(responsiveVoice.voiceSupport()) {
            responsiveVoice.speak("안녕하세요. 미러에요!","Korean Female");
          }
    	$scope.focus = "default";
	},
	whatCanISay : function($scope){
		console.debug("Here is a list of commands...");
        if(responsiveVoice.voiceSupport()) {
          responsiveVoice.speak("다음은 이용 가능한 메뉴입니다.","Korean Female");
        }
        $scope.focus = "commands";
	}
};