--
-- PostgreSQL database dump
--


-- Dumped from database version 18.3
-- Dumped by pg_dump version 18.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: cube; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS cube WITH SCHEMA public;


--
-- Name: EXTENSION cube; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION cube IS 'data type for multidimensional cubes';


--
-- Name: earthdistance; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS earthdistance WITH SCHEMA public;


--
-- Name: EXTENSION earthdistance; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION earthdistance IS 'calculate great-circle distances on the surface of the Earth';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: app_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_user (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    email character varying(255),
    full_name character varying(255),
    password_hash character varying(255),
    phone_number character varying(12),
    email_verified_at timestamp(6) with time zone,
    phone_number_verified_at timestamp(6) with time zone,
    status character varying(255),
    user_role character varying(255) NOT NULL,
    CONSTRAINT app_user_status_check CHECK (((status)::text = ANY ((ARRAY['INACTIVE'::character varying, 'ACTIVE'::character varying])::text[]))),
    CONSTRAINT app_user_user_role_check CHECK (((user_role)::text = ANY ((ARRAY['USER'::character varying, 'ADMIN'::character varying])::text[])))
);


--
-- Name: car; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.car (
    id uuid NOT NULL,
    carte_grise_url character varying(255),
    chassis_number character varying(255),
    plate_number character varying(255),
    registered_at timestamp(6) with time zone,
    status character varying(255),
    user_id uuid,
    verification_attempts integer NOT NULL,
    verified_at timestamp(6) with time zone,
    blocked_at timestamp(6) with time zone,
    deleted_at timestamp(6) with time zone,
    CONSTRAINT car_status_check CHECK (((status)::text = ANY (ARRAY[('UNVERIFIED'::character varying)::text, ('VERIFIED'::character varying)::text, ('BLOCKED'::character varying)::text])))
);


--
-- Name: notification_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notification_log (
    id uuid NOT NULL,
    is_sent boolean,
    reservation_id uuid,
    sent_at timestamp(6) without time zone,
    channel character varying(255),
    type character varying(255),
    CONSTRAINT notification_log_channel_check CHECK (((channel)::text = ANY ((ARRAY['SMS'::character varying, 'WHATSAPP'::character varying, 'IN_APP'::character varying])::text[]))),
    CONSTRAINT notification_log_type_check CHECK (((type)::text = ANY ((ARRAY['CONFIRMATION'::character varying, 'OTP'::character varying, 'REMINDER'::character varying, 'NO_SHOW_WARNING'::character varying, 'PORT_DISRUPTION'::character varying, 'CAR_REMOVED'::character varying, 'CAR_VERIFIED'::character varying, 'CAR_VERIFICATION_FAILED'::character varying, 'CHECK_IN'::character varying])::text[])))
);


--
-- Name: otp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.otp (
    id uuid NOT NULL,
    code character varying(255),
    created_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone,
    purpose character varying(255),
    reservation_id uuid,
    status character varying(255),
    user_id uuid,
    verified_at timestamp(6) with time zone,
    CONSTRAINT otp_purpose_check CHECK (((purpose)::text = ANY (ARRAY[('ACCOUNT_PHONE_VERIFICATION'::character varying)::text, ('RESERVATION_CONFIRMATION'::character varying)::text, ('EMAIL_VERIFICATION'::character varying)::text]))),
    CONSTRAINT otp_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'VERIFIED'::character varying, 'EXPIRED'::character varying, 'INVALIDATED'::character varying])::text[])))
);


--
-- Name: port; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.port (
    id uuid NOT NULL,
    access_identifier character varying(6),
    name character varying(255),
    status character varying(255),
    station_id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    CONSTRAINT port_status_check CHECK (((status)::text = ANY (ARRAY[('AVAILABLE'::character varying)::text, ('EXPIRING_SOON'::character varying)::text, ('OUT_OF_SERVICE'::character varying)::text])))
);


--
-- Name: refresh_token; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refresh_token (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone,
    is_revoked boolean,
    token character varying(255) NOT NULL,
    user_id uuid NOT NULL
);


--
-- Name: reservation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reservation (
    id uuid NOT NULL,
    cancellation_reason character varying(255),
    car_id uuid,
    checked_in_at timestamp(6) with time zone,
    contact_number character varying(255),
    created_at timestamp(6) with time zone,
    duration smallint,
    end_time timestamp(6) with time zone,
    late_cancel boolean NOT NULL,
    penalty_type character varying(255),
    penalty_waived boolean NOT NULL,
    port_id uuid,
    reservation_status character varying(255),
    start_time timestamp(6) with time zone,
    user_id uuid,
    cancelled_at timestamp(6) with time zone,
    CONSTRAINT reservation_cancellation_reason_check CHECK (((cancellation_reason)::text = ANY ((ARRAY['USER_CANCELLED'::character varying, 'PORT_OUT_OF_SERVICE'::character varying, 'CAR_REMOVED'::character varying])::text[]))),
    CONSTRAINT reservation_penalty_type_check CHECK (((penalty_type)::text = ANY ((ARRAY['NO_PENALTY'::character varying, 'NO_SHOW_PENALTY'::character varying, 'LATE_CANCEL_PENALTY'::character varying])::text[]))),
    CONSTRAINT reservation_reservation_status_check CHECK (((reservation_status)::text = ANY ((ARRAY['PENDING_OTP'::character varying, 'CONFIRMED'::character varying, 'CANCELLED'::character varying, 'CHECKED_IN'::character varying, 'COMPLETED'::character varying, 'NO_SHOW'::character varying, 'EXPIRED'::character varying])::text[])))
);


--
-- Name: station; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.station (
    station_id uuid NOT NULL,
    city character varying(255),
    created_at timestamp(6) with time zone,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    name character varying(255),
    town character varying(255)
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    email character varying(255),
    full_name character varying(255),
    password_hash character varying(255),
    phone_number character varying(255)
);


--
-- Name: app_user app_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_pkey PRIMARY KEY (id);


--
-- Name: car car_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.car
    ADD CONSTRAINT car_pkey PRIMARY KEY (id);


--
-- Name: notification_log notification_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification_log
    ADD CONSTRAINT notification_log_pkey PRIMARY KEY (id);


--
-- Name: otp otp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.otp
    ADD CONSTRAINT otp_pkey PRIMARY KEY (id);


--
-- Name: port port_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.port
    ADD CONSTRAINT port_pkey PRIMARY KEY (id);


--
-- Name: refresh_token refresh_token_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT refresh_token_pkey PRIMARY KEY (id);


--
-- Name: reservation reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_pkey PRIMARY KEY (id);


--
-- Name: station station_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.station
    ADD CONSTRAINT station_pkey PRIMARY KEY (station_id);


--
-- Name: refresh_token ukr4k4edos30bx9neoq81mdvwph; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT ukr4k4edos30bx9neoq81mdvwph UNIQUE (token);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: port fknj835ewblqp7ss0y7rpq1crcp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.port
    ADD CONSTRAINT fknj835ewblqp7ss0y7rpq1crcp FOREIGN KEY (station_id) REFERENCES public.station(station_id);


