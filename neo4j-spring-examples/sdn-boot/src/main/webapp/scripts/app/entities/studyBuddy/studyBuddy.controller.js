'use strict';

angular.module( 'registrarApp' )
    .controller( 'StudyBuddyController', function ( $scope, $state, StudyBuddy )
    {
        $scope.studyBuddies = [];

        $scope.loadAll = function ()
        {
            StudyBuddy.query( function ( result )
            {
                $scope.studyBuddies = result;
            } );
        };
        $scope.loadAll();

        $scope.create = function ()
        {

            console.log( "saving StudyBuddy" );
            //$scope.truncate($scope.StudyBuddy);
            console.log( $scope.studyBuddy );

            StudyBuddy.save( $scope.studyBuddy,
                function ()
                {
                    $scope.loadAll();
                    $scope.clear();
                    $( '#saveStudyBuddyModal' ).modal( 'hide' );
                } );
        };

        $scope.update = function ( id )
        {
            $scope.studyBuddy = StudyBuddy.get( {id: id} );
            $( '#saveStudyBuddyModal' ).modal( 'show' );
        };

        $scope.delete = function ( id )
        {
            $scope.studyBuddy = StudyBuddy.get( {id: id} );
            $( '#deleteStudyBuddyConfirmation' ).modal( 'show' );
        };

        $scope.confirmDelete = function ( id )
        {
            StudyBuddy.delete( {id: id},
                function ()
                {
                    var popup = $( '#deleteStudyBuddyConfirmation' );
                    popup.on( 'hidden.bs.modal', function ( e )
                    {
                        $scope.loadAll();
                        $state.transitionTo( 'StudyBuddy' );
                    } );
                    $scope.clear();
                    popup.modal( 'hide' );
                } );
        };

        $scope.clear = function ()
        {
            $scope.studyBuddy = {};
        };

        $scope.objectifyStudentOne = function ()
        {
            console.log( "objectified" );
            $scope.studyBuddy.studentOne = angular.fromJson( $scope.studyBuddy.studentOne );
        };

        $scope.objectifyStudentTwo = function ()
        {
            console.log( "objectified" );
            $scope.studyBuddy.studentTwo = angular.fromJson( $scope.studyBuddy.studentTwo );
        };

        $scope.objectifyClass = function ()
        {
            console.log( "objectified" );
            $scope.studyBuddy.class = angular.fromJson( $scope.studyBuddy.class );
        };
    } );
