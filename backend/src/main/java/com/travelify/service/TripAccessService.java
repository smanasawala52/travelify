package com.travelify.service;

import com.travelify.exception.ForbiddenOperationException;
import com.travelify.exception.ValidationException;
import com.travelify.model.AgentTrip;
import com.travelify.model.Role;
import com.travelify.model.Service;
import com.travelify.model.User;
import org.springframework.stereotype.Component;

/**
 * Shared ownership / role checks for trip and service mutations.
 */
@Component
public class TripAccessService {

    public void requireAdmin(User actor) {
        requireActor(actor);
        if (actor.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Admin access required");
        }
    }

    public void requireAgentOrAdmin(User actor) {
        requireActor(actor);
        if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.AGENT) {
            throw new ForbiddenOperationException("Agent or admin access required");
        }
    }

    public void requireTripOwnerOrAdmin(User actor, AgentTrip trip) {
        requireActor(actor);
        if (actor.getRole() == Role.ADMIN) {
            return;
        }
        if (actor.getRole() == Role.AGENT
                && trip.getAgent() != null
                && actor.getId().equals(trip.getAgent().getId())) {
            return;
        }
        throw new ForbiddenOperationException("Only the owning agent or an admin can modify this trip");
    }

    public void requireServiceOwnerOrAdmin(User actor, Service service) {
        requireActor(actor);
        if (actor.getRole() == Role.ADMIN) {
            return;
        }
        if (actor.getRole() == Role.AGENT
                && service.getProvider() != null
                && actor.getId().equals(service.getProvider().getId())) {
            return;
        }
        throw new ForbiddenOperationException("Only the owning provider or an admin can modify this service");
    }

    public boolean canViewTrip(User actor, AgentTrip trip) {
        if (trip.getStatus() == com.travelify.model.PublishStatus.PUBLISHED) {
            return true;
        }
        if (actor == null) {
            return false;
        }
        if (actor.getRole() == Role.ADMIN) {
            return true;
        }
        return actor.getRole() == Role.AGENT
                && trip.getAgent() != null
                && actor.getId().equals(trip.getAgent().getId());
    }

    public void assertCanViewTrip(User actor, AgentTrip trip) {
        if (!canViewTrip(actor, trip)) {
            throw new ForbiddenOperationException("You do not have access to this trip");
        }
    }

    private void requireActor(User actor) {
        if (actor == null) {
            throw new ForbiddenOperationException("Authentication required");
        }
        if (Boolean.FALSE.equals(actor.getIsActive())) {
            throw new ForbiddenOperationException("Account is deactivated");
        }
    }

    public User requireUser(User actor) {
        if (actor == null) {
            throw new ValidationException("Authenticated user is required");
        }
        return actor;
    }
}
