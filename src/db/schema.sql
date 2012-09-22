--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;



--Create the tables

CREATE SEQUENCE method_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE methods (
    file_name character varying(255),
    package_name character varying(255),
    class_type character varying(255),
    method_name character varying(255),
    parameters text[],
    start_line integer,
    end_line integer,
    id integer NOT NULL PRIMARY KEY DEFAULT NEXTVAL('method_id_seq'::regclass)
);

ALTER SEQUENCE method_id_seq OWNED BY methods.id;

------------

CREATE SEQUENCE invokes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE invokes (
    caller integer NOT NULL,
    callee integer NOT NULL,
    id integer NOT NULL PRIMARY KEY DEFAULT NEXTVAL('invokes_id_seq'::regclass)
);

ALTER SEQUENCE invokes_id_seq OWNED BY invokes.id;

---------------

CREATE TABLE properties (
    repository character varying(255),
    commit_id character varying(255)
);

------------

CREATE TABLE users (
    email character varying(255),
    twitter character varying(255),
    twitter_accesstoken character varying(255),
    twitter_accesstokensecret character varying(255)
);
