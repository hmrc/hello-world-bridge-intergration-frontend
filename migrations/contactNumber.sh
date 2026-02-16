#!/bin/bash

echo ""
echo "Applying migration contactNumber"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /contactNumber                  controllers.contactNumberController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /contactNumber                  controllers.contactNumberController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changecontactNumber                        controllers.contactNumberController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changecontactNumber                        controllers.contactNumberController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "contactNumber.title = contactNumber" >> ../conf/messages.en
echo "contactNumber.heading = contactNumber" >> ../conf/messages.en
echo "contactNumber.checkYourAnswersLabel = contactNumber" >> ../conf/messages.en
echo "contactNumber.error.nonNumeric = Enter your contactNumber using numbers" >> ../conf/messages.en
echo "contactNumber.error.required = Enter your contactNumber" >> ../conf/messages.en
echo "contactNumber.error.wholeNumber = Enter your contactNumber using whole numbers" >> ../conf/messages.en
echo "contactNumber.error.outOfRange = contactNumber must be between {0} and {1}" >> ../conf/messages.en
echo "contactNumber.change.hidden = contactNumber" >> ../conf/messages.en

echo "Migration contactNumber completed"
