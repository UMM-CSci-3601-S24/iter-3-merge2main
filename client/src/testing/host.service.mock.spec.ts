import { MockHostService } from './host.service.mock';

describe('MockHostService', () => {
  let mockHostService: MockHostService;

  beforeEach(() => {
    mockHostService = new MockHostService();
  });

  it('should return the correct EndedHunt for the given id', (done) => {
    const expectedEndedHunt = MockHostService.testEndedHunts[0];
    const id = expectedEndedHunt.startedHunt._id;

    mockHostService.getEndedHuntById(id).subscribe((endedHunt) => {
      expect(endedHunt).toEqual(expectedEndedHunt);
      done();
    });
  });
});
