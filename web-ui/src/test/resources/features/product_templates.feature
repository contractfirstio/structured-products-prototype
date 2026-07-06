@e2e
Feature: Product template management

  Scenario: Create product template from list
    Given I am on the templates admin page
    When I create a template with description "Created by end-to-end test"
    Then the current template should appear in the list

  Scenario: Edit existing product template from list
    Given I am on the templates admin page
    And I have created a template with description "Created by end-to-end test"
    When I open the editor for the current template
    And I update the description to "Updated by end-to-end test"
    And I save the template
    Then I should see the saved template confirmation
    And the template list should show the updated description

  Scenario: Delete product template from list
    Given I am on the templates admin page
    And I have created a template with description "Created by end-to-end test"
    When I delete the current template from the list
    Then the current template should no longer appear in the list
