@e2e
Feature: Vaadin application shell

  Scenario: Application serves Vaadin UI
    Given I open the application home page
    Then I should see the Vaadin app layout
    And I should see the side navigation
    And I should see "Demo Environment"
