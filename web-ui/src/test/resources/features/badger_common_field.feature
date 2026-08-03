@e2e
Feature: Badger common field catalogue

  Scenario: Badger is available in the common fields catalogue
    Given I am on the templates admin page
    And I have created a template with description "Created by end-to-end test"
    When I open the editor for the current template
    And I open the common fields section
    Then I should see the common field "Badger" in category "Market & Technical" with type "String"
    And the common field "Badger" should not be included

  Scenario: Badger has the usual companion settings
    Given I am on the templates admin page
    And I have created a template with description "Created by end-to-end test"
    When I open the editor for the current template
    And I open the common fields section
    Then I should see the common field "Badger - Option"
    And I should see the common field "Badger - In email conf"
    And I should see the common field "Badger - BEX Logic"

  Scenario: Include Badger and keep it after save
    Given I am on the templates admin page
    And I have created a template with description "Created by end-to-end test"
    And I have included the common field "Badger" on the current template
    When I open the editor for the current template
    And I open the common fields section
    Then the common field "Badger" should be included
