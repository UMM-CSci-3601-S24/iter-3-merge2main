import { JoinHuntPage } from '../support/join-hunt.po';

const page = new JoinHuntPage();

describe('Join Hunt', () => {
  beforeEach(() => page.navigateTo());

  it('should display the right title for join hunt page', () => {
    page.getJoinHuntTitle().contains('Type Invite Code');
  });

  it('should display the access code input field', () => {
    page.getAccessCodeInputField().should('exist');
  });

  it('should display the join hunt button', () => {
    page.getJoinHuntButton().should('exist');
    page.getJoinHuntButton().contains('Join Hunt');
  });

  it('should display the join hunt button as disabled with no access code', () => {
    page.getAccessCodeInputField().should('exist');
    page.getAccessCodeInputField().should('have.value', '');
    page.getJoinHuntButton().should('exist');
    page.getJoinHuntButton().should('have.class', 'mat-mdc-button-disabled');
    // This check if the JoinHuntButton is disabled if there are no access code.
  });

  it('should display the join hunt button as disabled, warning message with invalid access code', () => {
    page.getAccessCodeInput(1).type('1');
    page.getAccessCodeInput(2).type('2');
    page.getAccessCodeInput(3).type('3');
    page.getAccessCodeInput(4).type('4');
    page.getJoinHuntButton().should('have.class', 'mat-mdc-button-disabled');
    // This will check if the JoinHuntButton is disabled if invalid access code is entered.
  });

  it('should display the join hunt button as enabled, no warning message with valid access code', () => {
    cy.visit(`/hosts/`);
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    cy.wait(1000);
    page.selectTeamNumber(1);
    page.clickHunterProceedButton();
    cy.wait(1000);

    page.getAccessCode();
    // Those above will navigate to the Hunt, begin it

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunters/`);
      for (let i = 0; i < accessCode.length; i++) {
        page.getAccessCodeInput(i + 1).type(accessCode.toString().charAt(i));
      }
    });
    page.getJoinHuntButton().should('not.have.class', 'mat-mdc-button-disabled');
    // This will check if the JoinHuntButton is enabled if valid access code (6 digit) is entered.
  });

  it('should not allowed letter in the access code input field', () => {
    // page.getAccessCodeInputField().should('exist');
    page.getAccessCodeInput(1).first().type('a');
    page.getAccessCodeInput(1).first().should('have.value', '');
    page.getAccessCodeInput(1).first().type('1');
    page.getAccessCodeInput(1).first().should('have.value', '1');

    page.getAccessCodeInput(2).type('n');
    page.getAccessCodeInput(2).should('have.value', '');
    page.getAccessCodeInput(2).type('2');
    page.getAccessCodeInput(2).should('have.value', '2');
  });

  it('should navigate to the home app page when the home button is clicked', () => {
    cy.url().should('match', /\/hunters$/);
    page.getHomeButton().click();
    cy.url().should('match', /\/$/);
  });

  it('should navigate to home page then go back to join hunt page', () => {
    page.getHomeButton().click();
    cy.url().should('match', /\/$/);

    page.getHunterButton().click();
    cy.url().should('match', /\/hunters$/);
  });

  it('should navigate to the right hunter view page when join hunt is clicked with valid access code', () => {
    cy.visit(`/hosts/`);
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    cy.wait(500);
    page.selectTeamNumber(1);
    page.clickHunterProceedButton();
    cy.wait(500);

    page.getAccessCode();

    //     // Those above will navigate to the Hunt, begin it

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunters/`);
      for (let i = 0; i < accessCode.length; i++) {
        page.getAccessCodeInput(i + 1).type(accessCode.toString().charAt(i));
      }
    });
    page.getJoinHuntButton().should('not.have.class', 'mat-mdc-button-disabled');
    page.getJoinHuntButton().click();
    // This will check if the JoinHuntButton is enabled if valid access code (6 digit) is entered.

    cy.wait(500);
    cy.url().should('match', /\/hunter-view\/\d+\/teams$/);
  });
});
