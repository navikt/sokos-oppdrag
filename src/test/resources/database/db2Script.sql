DROP TABLE IF EXISTS T_FAGGRUPPE;
CREATE TABLE T_FAGGRUPPE
(
    KODE_FAGGRUPPE         CHAR(8)                             not null,
    NAVN_FAGGRUPPE         CHAR(50)                            not null,
    SKATTEPROSENT          SMALLINT                            not null,
    OPPGJORSORDNING        CHAR(2)                             not null,
    ANT_VENTEDAGER         SMALLINT                            not null,
    KODE_KLASSE_FEIL       CHAR(20)                            not null,
    KODE_KLASSE_JUST       CHAR(20)                            not null,
    KODE_KLASSE_MOTP_FEIL  CHAR(20)                            not null,
    KODE_KLASSE_MOTP_TREKK CHAR(20)                            not null,
    KODE_KLASSE_MOTP_INNKR CHAR(20)                            not null,
    KODE_DESTINASJON       CHAR(4)                             not null,
    KODE_RESK_OPPDRAG      CHAR(7)                             not null,
    ONLINE_BEREGNING       CHAR(1)                             not null,
    KODE_PENSJON           CHAR(1)                             not null,
    OREAVRUND              CHAR(1)                             not null,
    SAMORD_BEREGNING       CHAR(1)                             not null,
    PRIORITET_TABELL       SMALLINT                            not null,
    ANT_MND_TILBAKE        SMALLINT                            not null,
    BRUKERID               CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG            TIMESTAMP(6) default CURRENT TIMESTAMP not null,
    AUTO_TIL_RESKONTRO     CHAR(1),
    KODE_VALG_SKATT        CHAR(4),
    SKATT_TIDL_AAR         CHAR(4),
    SPLITT_PERIODE         CHAR(1),
    REDUSER_KRAVGRUNNLAG   CHAR(1)
);

insert into T_FAGGRUPPE (KODE_FAGGRUPPE, NAVN_FAGGRUPPE, SKATTEPROSENT, OPPGJORSORDNING, ANT_VENTEDAGER, KODE_KLASSE_FEIL, KODE_KLASSE_JUST, KODE_KLASSE_MOTP_FEIL, KODE_KLASSE_MOTP_TREKK,
                         KODE_KLASSE_MOTP_INNKR, KODE_DESTINASJON, KODE_RESK_OPPDRAG, ONLINE_BEREGNING, KODE_PENSJON, OREAVRUND, SAMORD_BEREGNING, PRIORITET_TABELL, ANT_MND_TILBAKE, BRUKERID,
                         TIDSPKT_REG, AUTO_TIL_RESKONTRO, KODE_VALG_SKATT, SKATT_TIDL_AAR, SPLITT_PERIODE, REDUSER_KRAVGRUNNLAG)
values ('BA      ', 'Barnetrygd                                        ', 0, '81', 0, 'KL_KODE_FEIL_BA     ', 'KL_KODE_JUST_BA     ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'BA     ', 'J', ' ', 'J', 'F', 99, 0, 'POPPSETT', '2010-09-18 15:20:14.172212', 'J', null, null, 'N', ' '),
       ('BIDRINKR', 'Bidragsreskontro                                  ', 0, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'BRBATCH', 'N', ' ', 'N', 'O', 99, 0, 'POPPSETT', '2005-09-12 10:57:03.833034', 'J', null, null, 'N', null),
       ('BIDRRESK', 'Bidragsreskontro                                  ', 0, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'BRBATCH', 'J', ' ', 'J', 'O', 99, 0, 'POPPSETT', '2005-09-12 10:57:03.833034', 'J', null, null, 'N', null),
       ('BISYS   ', 'Bidragssystemet                                   ', 0, '  ', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB05', 'KRAV   ', 'J', ' ', 'J', 'O', 99, 0, 'POPPSETT', '2005-11-09 11:42:30.205928', 'J', null, null, 'N', null),
       ('EFOG    ', 'Enslig forsørger - Overgangsstønad                ', 50, '  ', 0, 'KL_KODE_FEIL_EFOG   ', 'KL_KODE_JUST_EFOG   ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'EFOG   ', 'J', ' ', 'J', 'F', 2, 0, 'TOB-269 ', '2019-02-07 22:26:45.660570', 'J', 'UBGR', '    ', 'N', null),
       ('EFVM    ', 'Enslig forsørger - Virkemidler                    ', 0, '81', 0, 'KL_KODE_FEIL_PEN    ', 'KL_KODE_JUST_PEN    ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'PEN    ', 'J', 'P', 'J', 'F', 99, 0, 'POPPSETT', '2009-10-29 13:10:12.908051', 'J', null, null, 'N', null),
       ('ERESEPT ', 'Apotek og bandasjistoppgjør                       ', 0, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'BEBATCH', 'N', ' ', 'N', 'O', 99, 0, 'POPPSETT', '2009-03-12 14:12:54.557526', 'J', null, null, 'N', null),
       ('FRIKORT ', 'HELSETJENESTER FRIKORT TAK 1 OG 2                 ', 0, '81', 0, 'KL_KODE_FEIL_HELFO  ', 'KL_KODE_JUST_HELFO  ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'BEBATCH', 'N', ' ', 'N', 'O', 99, 0, 'POPPSETT', '2009-12-08 12:55:51.783477', 'J', null, null, 'N', null),
       ('GH      ', 'Grunn og hjelpestønad                             ', 0, '81', 0, 'KL_KODE_FEIL_GH     ', 'KL_KODE_JUST_GH     ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'GHBATCH', 'J', 'P', 'J', 'F', 99, 0, 'POPPSETT', '2008-05-21 21:28:48.779268', 'J', null, null, 'N', null),
       ('GS      ', 'Gravferdsstønad                                   ', 0, '81', 0, 'KL_KODE_FEIL_GS     ', 'KL_KODE_JUST_GS     ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'GS     ', 'J', ' ', 'J', 'F', 99, 0, 'POPPSETT', '2010-09-18 15:20:14.122983', 'J', null, null, 'N', null),
       ('HELSEREF', 'Refusjon helseutgifter                            ', 0, '81', 0, 'KL_KODE_FEIL_HELSER ', 'KL_KODE_JUST_HELSER ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'ANDRUTB', 'J', ' ', 'N', 'O', 97, 0, 'OS16HL4 ', '2016-11-26 00:22:04.233997', 'J', null, null, 'N', null),
       ('INGENOPP', 'Ikke trekkoppgjør                                 ', 0, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'INGENOP', 'N', ' ', 'J', 'L', 99, 0, 'POPPSETT', '2008-01-07 11:40:46.603631', 'J', null, null, 'N', null),
       ('INNT    ', 'Inntektsytelser                                   ', 50, '  ', 0, 'KL_KODE_FEIL_INNT   ', 'KL_KODE_JUST_INNT   ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'INNT   ', 'J', 'U', 'J', 'F', 3, 0, 'OSR1401 ', '2013-11-14 08:41:44.397783', 'J', 'UBGR', null, 'N', null),
       ('IT26    ', 'Tidsbegrenset uførestønad                         ', 50, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'TUBATCH', 'J', 'U', 'J', 'O', 6, 0, 'POPPSETT', '2003-06-23 11:10:37.109028', 'J', null, '    ', 'N', null),
       ('IT27    ', 'Supplerende stønad                                ', 30, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST_PEN    ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'SUBATCH', 'J', 'P', 'J', 'F', 4, 0, 'POPPSETT', '2005-10-03 07:59:10.788619', 'J', null, null, 'N', null),
       ('KORTTID ', 'Korttidsytelser                                   ', 50, '81', 0, 'KL_KODE_FEIL_KORTTID', 'KL_KODE_JUST_KORTTID', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'KORTTID', 'J', ' ', 'J', 'F', 5, 1, 'OS16HL4 ', '2016-11-26 00:21:16.193359', 'J', 'UFOR', '    ', 'J', 'J'),
       ('KREDDISP', 'Kreditoroppgjør                                   ', 0, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'KREDREF', 'N', ' ', 'J', 'L', 99, 0, 'POPPSETT', '2008-01-03 13:39:11.716364', 'J', null, null, 'N', null),
       ('KORONA  ', 'Korona                                            ', 50, '81', 0, 'KL_KODE_FEIL_KORONA ', 'KL_KODE_JUST_KORTTID', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'KORTTID', 'J', ' ', 'J', 'F', 7, 9, 'TOB-1074', '2020-03-31 08:18:53.983505', 'J', 'UFOR', '    ', 'J', 'J'),
       ('KREDREF ', 'Kreditoroppgjør                                   ', 0, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'KREDREF', 'J', ' ', 'J', 'L', 99, 0, 'POPPSETT', '2006-01-09 11:34:54.545237', 'J', null, null, 'N', null),
       ('KS      ', 'Kontantstøtte                                     ', 0, '81', 0, 'KL_KODE_FEIL_KS     ', 'KL_KODE_JUST_KS     ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'KS     ', 'J', ' ', 'J', 'F', 99, 0, 'POPPSETT', '2010-09-18 15:20:14.172577', 'J', null, null, 'N', null),
       ('KTPOST  ', 'Korttid, memopostering                            ', 0, '81', 0, 'KL_KODE_FEIL_KORTTID', 'KL_KODE_JUST_KTPOST ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'KTPOST ', 'J', ' ', 'J', 'O', 99, 0, 'OS16HL4 ', '2016-11-26 00:21:16.194897', 'J', null, null, 'N', null),
       ('MSKATT  ', 'Skatteoppgjør - manuelle posteringer              ', 0, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'SKATOPP', 'J', ' ', 'J', 'O', 99, 0, 'POPPSETT', '2012-01-04 12:12:17.609158', 'J', null, null, 'N', null),
       ('NEGKRED ', 'Negative kreditoroppgjør                          ', 0, '81', 0, 'KL_KODE_FEIL_TREKK  ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'KREDREF', 'N', ' ', 'J', 'L', 99, 0, 'POPPSETT', '2010-09-11 10:44:10.158339', 'J', null, null, 'N', null),
       ('NEGSKATT', 'Negative skatteoppgjør                            ', 0, '81', 0, 'KL_KODE_FEIL_SKATT  ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'SKATOPP', 'J', ' ', 'J', 'L', 99, 0, 'POPPSETT', '2010-09-11 10:44:10.220580', 'J', null, null, 'N', null),
       ('PEN     ', 'Pensjoner                                         ', 30, '81', 0, 'KL_KODE_FEIL_PEN    ', 'KL_KODE_JUST_PEN    ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'PEN    ', 'J', 'P', 'J', 'F', 1, 0, 'POPPSETT', '2008-10-02 09:39:11.759635', 'J', 'UTAP', null, 'N', null),
       ('PENPOST ', 'Memoposteringer pensjon                           ', 30, '81', 0, 'KL_KODE_FEIL_PEN    ', 'KL_KODE_JUST_PEN    ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'PEN    ', 'J', ' ', 'N', 'O', 98, 0, 'POPPSETT', '2008-11-24 15:54:34.184916', 'J', null, null, 'N', null),
       ('PREDATOR', 'Interne trekk til TI                              ', 0, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'PRED   ', 'J', ' ', 'J', 'L', 99, 0, 'POPPSETT', '2006-01-09 11:34:54.545571', 'J', null, null, 'N', null),
       ('REFARBG ', 'Refusjon Arbeidsgiver                             ', 0, '81', 0, 'KL_KODE_FEIL_REFARBG', 'KL_KODE_JUST_REFARBG', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'REFARBG', 'J', ' ', 'J', 'O', 99, 0, 'OS16HL4 ', '2016-11-26 00:21:16.191865', 'J', null, null, 'N', null),
       ('REFUTG  ', 'Refusjon utgifter                                 ', 0, '81', 0, 'KL_KODE_FEIL_REFUTG ', 'KL_KODE_JUST_REFUTG ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'REFUTG ', 'J', ' ', 'J', 'F', 99, 1, 'OS16HL4 ', '2016-11-26 00:21:16.196337', 'J', null, null, 'N', 'N'),
       ('SAPO01  ', 'Behandleroppgjør                                  ', 0, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'BEBATCH', 'J', ' ', 'N', 'O', 99, 0, 'POPPSETT', '2005-10-22 15:43:11.664751', 'J', null, null, 'N', null),
       ('SKATOPPG', 'Skatteoppgjør                                     ', 0, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'SKATOPP', 'N', ' ', 'J', 'L', 99, 0, 'POPPSETT', '2006-01-09 11:34:54.544595', 'J', null, null, 'N', null),
       ('SRNODU  ', 'Nødutbetalinger sentralisert regnskap             ', 0, '81', 0, 'KL_KODE_FEIL_SR     ', 'KL_KODE_JUST_SR     ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'SRNODU ', 'N', ' ', 'N', 'O', 99, 0, 'POPPSETT', '2011-02-18 10:27:41.861131', 'J', null, null, 'N', null),
       ('SRPOST  ', 'Memoposteringer sentralisert regnskap             ', 0, '81', 0, 'KL_KODE_FEIL_SR     ', 'KL_KODE_JUST_SR     ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'SRPOST ', 'J', ' ', 'N', 'O', 99, 0, 'POPPSETT', '2011-02-18 10:27:41.845422', 'J', null, null, 'N', null),
       ('SVALSKAT', 'Svalbardskatt                                     ', 0, '81', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'SKATOPP', 'N', ' ', 'J', 'L', 99, 0, 'POPPSETT', '2008-09-30 20:34:42.624261', 'J', null, null, 'N', null),
       ('TB      ', 'Tilbakekreving                                    ', 0, '81', 0, 'KL_KODE_FEIL_TB     ', 'KL_KODE_JUST_TB     ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'TB     ', 'J', ' ', 'J', 'O', 99, 0, 'POPPSETT', '2011-01-18 13:49:51.836416', 'J', null, null, 'N', null),
       ('KORONA2 ', 'Korona engangsbeløp                               ', 50, '81', 0, 'KL_KODE_FEIL_KORONA ', 'KL_KODE_JUST_KORTTID', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'KORTTID', 'J', ' ', 'J', 'F', 7, 0, 'TOB-1225', '2020-11-11 14:29:18.655507', 'J', 'UFOR', '    ', 'N', 'N'),
       ('TBBT    ', 'Tilbakekreving, betalt                            ', 0, '81', 0, 'KL_KODE_FEIL_TB     ', 'KL_KODE_JUST_TB     ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'TB     ', 'J', ' ', 'N', 'O', 99, 0, 'OS16HL4 ', '2016-11-26 00:22:06.260507', 'J', null, null, 'N', null),
       ('TBKRAV  ', 'Tilbakekrevingskrav                               ', 0, '81', 0, 'KL_KODE_FEIL_KRAV_TB', 'KL_KODE_JUST_KRAV_TB', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OEBS', 'TB     ', 'N', ' ', 'J', 'L', 99, 0, 'POPPSETT', '2011-01-18 13:49:51.979071', 'J', null, null, 'N', null),
       ('UTBINNKR', 'Utbetaling for mye innkrevd                       ', 0, '  ', 0, 'KL_KODE_FEIL        ', 'KL_KODE_JUST        ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'ANDRUTB', 'J', ' ', 'N', 'F', 99, 0, 'OPOS592 ', '2013-08-21 16:09:30.511239', 'J', null, null, 'N', null),
       ('UTPOST  ', 'Memoposteringer Uføretrygd                        ', 50, '  ', 0, 'KL_KODE_FEIL_INNT   ', 'KL_KODE_JUST_INNT   ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'INNT   ', 'J', 'U', 'J', 'F', 98, 0, 'OSR1403 ', '2013-11-14 08:41:44.459156', 'J', null, null, 'N', null),
       ('YS      ', 'Menerstatning yrkesskade                          ', 0, '81', 0, 'KL_KODE_FEIL_YS     ', 'KL_KODE_JUST_YS     ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'YSBATCH', 'J', 'P', 'J', 'F', 99, 0, 'POPPSETT', '2008-05-21 21:28:48.764617', 'J', null, null, 'N', null),
       ('KORONA3 ', 'Forskudd til feilutbetaling og krav               ', 0, '81', 0, 'KL_KODE_FEIL_KORONA ', 'KL_KODE_JUST_KORTTID', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'KORTTID', 'J', ' ', 'J', 'O', 99, 0, 'TOB-1582', '2021-05-27 11:40:44.523591', 'J', null, '    ', 'N', 'N'),
       ('BARNBRIL', 'Barnebriller                                      ', 50, '81', 0, 'KL_KODE_FEIL_BARNBRI', 'KL_KODE_JUST_BARNBRI', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'ANDRUTB', 'J', ' ', 'J', 'F', 99, 0, 'TOB-2488', '2022-10-31 14:52:39.582449', 'J', null, null, 'N', 'N'),
       ('ARBYT   ', 'Arbeidsytelser                                    ', 50, '81', 0, 'KL_KODE_FEIL_ARBYT  ', 'KL_KODE_JUST_ARBYT  ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'ARBYT  ', 'J', ' ', 'J', 'F', 5, 1, 'TOB-1958', '2023-03-11 09:33:46.467106', 'J', 'UBGR', '    ', 'J', 'J'),
       ('OMSTILL ', 'Omstillingsstønad                                 ', 50, '  ', 0, 'KL_KODE_FEIL_OMSTILL', 'KL_KODE_JUST_OMSTILL', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'OMSTILL', 'J', ' ', 'J', 'F', 5, 0, 'TOB-2111', '2023-10-11 11:37:12.396427', 'J', null, null, 'N', 'J'),
       ('UNG     ', 'Ungdomsytelse                                     ', 50, '81', 0, 'KL_KODE_FEIL_KORTTID', 'KL_KODE_JUST_KORTTID', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'KORTTID', 'J', ' ', 'J', 'F', 5, 0, 'TOB-4122', '2024-09-27 10:01:14.146016', 'J', 'UFOR', '    ', 'J', 'J'),
       ('ARBTIL  ', 'Arbeidsytelser tilleggsstønad og tiltakspenger    ', 0, '81', 0, 'KL_KODE_FEIL_ARBYT  ', 'KL_KODE_JUST_ARBYT  ', 'TBMOTOBS            ', 'TBTREKK             ',
        'TBMOTFB             ', 'OB01', 'ARBYT  ', 'J', ' ', 'J', 'F', 5, 1, 'TOB-4247', '2024-10-28 12:59:46.444096', 'J', null, null, 'J', 'J');

DROP TABLE IF EXISTS T_FAGOMRAADE;
CREATE TABLE T_FAGOMRAADE
(
    KODE_FAGOMRAADE    CHAR(8)                             not null,
    NAVN_FAGOMRAADE    CHAR(50)                            not null,
    KODE_FAGGRUPPE     CHAR(8)                             not null,
    ANT_ATTESTANTER    SMALLINT                            not null,
    MAKS_AKT_OPPDRAG   SMALLINT                            not null,
    TPS_DISTRIBUSJON   CHAR(1)                             not null,
    SJEKK_OFFID        CHAR(1)                             not null,
    ANVISER            CHAR(3)                             not null,
    SJEKK_MOT_TPS      CHAR(1)                             not null,
    KODE_MOTREGNGRUPPE CHAR(4)                             not null,
    BRUKERID           CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG        TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

insert into T_FAGOMRAADE (KODE_FAGOMRAADE, NAVN_FAGOMRAADE, KODE_FAGGRUPPE, ANT_ATTESTANTER, MAKS_AKT_OPPDRAG, TPS_DISTRIBUSJON, SJEKK_OFFID, ANVISER, SJEKK_MOT_TPS, KODE_MOTREGNGRUPPE,
                          BRUKERID, TIDSPKT_REG)
values ('AAP     ', 'Arbeidsavklaringspenger                           ', 'ARBYT   ', 1, 99, 'J', 'J', 'N  ', 'J', 'MAAP', 'TOB-1958', '2023-03-11 09:33:46.578878'),
       ('AAPARENA', 'Arbeidsavklaringspenger                           ', 'ARBYT   ', 1, 99, 'J', 'J', 'N  ', 'J', 'MAAP', 'TOB-1958', '2023-03-11 09:33:46.494650'),
       ('AGPERM  ', 'Lønnskomp arbeidsgiver, permitterte               ', 'KORONA  ', 1, 99, 'N', 'J', '   ', 'J', 'MAGP', 'TOB1074C', '2020-05-04 08:04:02.621239'),
       ('BA      ', 'Barnetrygd                                        ', 'BA      ', 1, 5, 'J', 'J', '   ', 'J', 'MBA ', 'TOB-894 ', '2020-02-19 11:46:55.913233'),
       ('BARNBRIL', 'Barnebriller                                      ', 'BARNBRIL', 1, 32000, 'N', 'J', '   ', 'J', 'BARN', 'TOB-2342', '2022-09-06 08:57:52.385868'),
       ('BARNEPE ', 'Barnepensjon                                      ', 'PEN     ', 1, 1, 'J', 'J', 'N  ', 'J', 'MPBP', 'TOB-2212', '2023-01-25 14:28:42.107582'),
       ('BIDRINKR', 'Bidragsreskontro tilbakebetaling                  ', 'BIDRINKR', 1, 20, 'J', 'J', '   ', 'J', '    ', 'POPPSETT', '2005-09-12 10:57:03.833034'),
       ('BIDRRESK', 'Bidragsreskontro forskudd/bidrag                  ', 'BIDRRESK', 1, 20, 'J', 'J', '   ', 'J', '    ', 'POPPSETT', '2005-09-12 10:57:03.833034'),
       ('BISYS   ', 'Bidragssystemet                                   ', 'BISYS   ', 1, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2005-11-09 11:42:30.205928'),
       ('DP      ', 'Dagpenger                                         ', 'ARBYT   ', 1, 99, 'J', 'J', 'N  ', 'J', 'MDP ', 'TOB-1958', '2023-03-11 09:33:46.579507'),
       ('DPARENA ', 'Dagpenger                                         ', 'ARBYT   ', 1, 99, 'J', 'J', 'N  ', 'J', 'MDP ', 'TOB-1958', '2023-03-11 09:33:46.521207'),
       ('EFBT    ', 'Enslig forsørger - Barnetilsyn                    ', 'EFVM    ', 1, 2, 'J', 'J', '   ', 'J', 'MEBT', 'TOB-1167', '2021-06-14 08:54:41.931628'),
       ('EFOG    ', 'Enslig forsørger overgangsstønad                  ', 'EFOG    ', 1, 1, 'J', 'J', '   ', 'J', 'MOGN', 'TOB-1167', '2021-06-14 08:54:41.892162'),
       ('EFOGNY  ', 'Enslig forsørger - Overgangsstønad                ', 'EFOG    ', 1, 2, 'N', 'J', '   ', 'J', 'MOGN', 'TOB-269 ', '2014-01-07 13:22:37.006830'),
       ('EFSP    ', 'Enslig forsørger - Skolepenger                    ', 'EFVM    ', 1, 2, 'J', 'J', '   ', 'J', 'MEUT', 'TOB-1167', '2021-06-14 08:54:41.944431'),
       ('ERESEPT ', 'Apotek og bandasjistoppgjør                       ', 'ERESEPT ', 1, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2009-03-12 14:17:00.322312'),
       ('FORSKRAV', 'FORSKUDD til feilutbetaling                       ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1582', '2021-05-27 11:40:44.576320'),
       ('FORSKTBK', 'Forskudd tilbakekreving                           ', 'KORONA3 ', 1, 1, 'J', 'N', '   ', 'J', '    ', 'TOB-1582', '2021-05-27 11:40:44.552847'),
       ('FORSKUDD', 'Forskudd permit og dagpenger                      ', 'KORONA  ', 1, 5, 'J', 'J', '   ', 'J', 'MFOR', 'TOB-1074', '2020-03-31 08:18:54.047425'),
       ('FP      ', 'Foreldrepenger                                    ', 'KORTTID ', 1, 5, 'J', 'J', '   ', 'J', 'MKT1', 'OS16HL4 ', '2016-11-26 00:22:07.322796'),
       ('FPREF   ', 'Foreldrepenger, AG refusjon                       ', 'REFARBG ', 1, 10, 'J', 'J', '   ', 'J', 'MRA1', 'OS16HL4 ', '2016-11-26 00:17:58.122225'),
       ('FRIKORT1', 'Egenandelsrefusjon frikort tak 1                  ', 'FRIKORT ', 1, 30, 'N', 'J', '   ', 'J', 'MFRI', 'POPPSETT', '2009-12-08 12:57:10.254486'),
       ('FRISINN ', 'Kompensasjon inntektstap Frilanser og Næring      ', 'KORONA  ', 1, 1, 'J', 'J', '   ', 'J', 'MFRS', 'TOB1074C', '2020-05-04 08:04:02.674317'),
       ('GJENLEV ', 'Gjenlevendepensjon tilleggsstønader               ', 'PEN     ', 1, 1, 'J', 'J', 'N  ', 'J', 'GJTS', 'TOB-3926', '2024-09-13 13:53:28.314641'),
       ('HELSREF ', 'Helserefusjoner NAV                               ', 'HELSEREF', 1, 5, 'N', 'J', '   ', 'J', 'MHER', 'OS16HL4 ', '2016-11-26 00:22:04.235543'),
       ('INGENOPP', 'Ikke trekkoppgjør                                 ', 'INGENOPP', 0, 0, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2008-01-07 11:40:46.603631'),
       ('INTERNTR', 'Interne trekk                                     ', 'INTERNTR', 0, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2006-01-09 11:34:54.391381'),
       ('ITGS    ', 'Gravferdsstønad                                   ', 'GS      ', 1, 2, 'N', 'J', '   ', 'J', 'MGS ', 'POPPSETT', '2010-09-18 15:25:37.085662'),
       ('IT05    ', 'Barnetrygd                                        ', 'BA      ', 1, 2, 'J', 'J', '   ', 'J', 'MBA ', 'POPPSETT', '2010-09-18 15:25:37.122126'),
       ('IT18    ', 'Kontantstøtte                                     ', 'KS      ', 1, 1, 'N', 'J', '   ', 'J', 'MKS ', 'POPPSETT', '2010-09-18 15:25:37.122445'),
       ('IT24    ', 'Menerstatning Yrkesskade                          ', 'YS      ', 1, 1, 'N', 'J', '   ', 'J', 'MYS ', 'POPPSETT', '2008-10-01 17:37:20.514692'),
       ('IT26    ', 'Tidsbegrenset uførestønad                         ', 'IT26    ', 1, 5, 'N', 'J', '   ', 'J', 'MTU ', 'POPPSETT', '2003-06-23 11:10:37.109028'),
       ('IT27    ', 'Supplerende stønad                                ', 'IT27    ', 1, 2, 'N', 'J', '   ', 'J', 'MSU ', 'POPPSETT', '2005-10-03 07:59:10.788619'),
       ('IT28    ', 'Grunnstønad                                       ', 'GH      ', 1, 2, 'N', 'J', '   ', 'J', 'MGST', 'POPPSETT', '2008-10-01 17:37:20.517758'),
       ('IT29    ', 'Hjelpestønad                                      ', 'GH      ', 1, 3, 'N', 'J', '   ', 'J', 'MHST', 'POPPSETT', '2008-10-01 17:37:20.518632'),
       ('IT30BT  ', 'Enslig forsørger - Barnetilsyn                    ', 'EFVM    ', 1, 2, 'N', 'J', '   ', 'J', 'MEBT', 'POPPSETT', '2009-10-29 13:05:28.828529'),
       ('IT30FL  ', 'Enslig forsørger - Tilskott til flytting          ', 'EFVM    ', 1, 2, 'N', 'J', '   ', 'J', 'MEFL', 'POPPSETT', '2009-10-29 13:05:28.859418'),
       ('IT30OG  ', 'Enslig forsørger - Overgangsstønad                ', 'PEN     ', 1, 2, 'N', 'J', '   ', 'J', 'MEOG', 'POPPSETT', '2009-10-29 13:05:28.742915'),
       ('IT30UT  ', 'Enslig forsørger - Utdanningsstønad               ', 'EFVM    ', 1, 2, 'N', 'J', '   ', 'J', 'MEUT', 'POPPSETT', '2009-10-29 13:05:28.859104'),
       ('IT31    ', 'Gjenlevendepensjon Virkemidler                    ', 'PEN     ', 1, 3, 'N', 'J', '   ', 'J', 'MGVM', 'POPPSETT', '2008-10-01 17:37:20.519857'),
       ('IT32    ', 'Familiepleier Virkemidler                         ', 'PEN     ', 1, 2, 'N', 'J', '   ', 'J', 'MFVM', 'POPPSETT', '2008-10-01 17:37:20.520688'),
       ('KREDDISP', 'Kreditoroppgjør(disponerer)                       ', 'KREDDISP', 0, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2008-01-03 13:39:11.716364'),
       ('KREDREF ', 'Kreditoroppgjør(ordinær)                          ', 'KREDREF ', 0, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2006-01-09 11:34:54.545237'),
       ('KS      ', 'Kontantstøtte                                     ', 'KS      ', 1, 1, 'J', 'J', 'N  ', 'J', 'MKS ', 'TOB-2377', '2022-12-15 10:33:38.555810'),
       ('MAAP    ', 'Arbeidsavklaringspenger, manuell postering        ', 'ARBYT   ', 2, 5, 'J', 'J', 'N  ', 'J', 'MAAP', 'TOB-1958', '2023-03-11 09:33:46.581300'),
       ('MAAPAREN', 'Arbeidsavklaringspenger, manuell postering        ', 'ARBYT   ', 2, 5, 'J', 'J', 'N  ', 'J', 'MAAP', 'TOB-1958', '2023-03-11 09:33:46.523694'),
       ('MAGPERM ', 'Lønnskomp arbeidsgiver, permitterte - manuell     ', 'KORONA  ', 2, 5, 'N', 'J', '   ', 'J', 'MAGP', 'TOB1074C', '2020-05-04 08:04:02.658184'),
       ('MBA     ', 'Barnetrygd                                        ', 'BA      ', 2, 5, 'N', 'J', '   ', 'J', 'MBA ', 'TOB-894 ', '2020-02-19 11:46:55.932718'),
       ('MBARNEPE', 'Barnepensjon, manuell postering                   ', 'PEN     ', 2, 5, 'J', 'J', 'N  ', 'J', 'MPBP', 'TOB-2212', '2023-01-25 14:28:42.141476'),
       ('MDP     ', 'Dagpenger, manuell postering                      ', 'ARBYT   ', 2, 5, 'J', 'J', 'N  ', 'J', 'MDP ', 'TOB-1958', '2023-03-11 09:33:46.581881'),
       ('MDPARENA', 'Dagpenger, manuell postering                      ', 'ARBYT   ', 2, 5, 'J', 'J', 'N  ', 'J', 'MDP ', 'TOB-1958', '2023-03-11 09:33:46.524386'),
       ('MEFBT   ', 'Enslig forsørger - Barnetilsyn, manuell           ', 'EFVM    ', 2, 2, 'N', 'J', '   ', 'J', 'MEBT', 'TOB-1167', '2021-06-14 08:54:41.959319'),
       ('MEFOGNY ', 'Enslig forsørger - Overgangsstønad - Manuell      ', 'EFOG    ', 2, 4, 'N', 'J', '   ', 'J', 'MOGN', 'TOB-269 ', '2014-01-07 13:22:37.187261'),
       ('MEFSP   ', 'Enslig forsørger - Skolepenger, manuell           ', 'EFVM    ', 2, 2, 'N', 'J', '   ', 'J', 'MEUT', 'TOB-1167', '2021-06-14 08:54:41.972856'),
       ('MEFTB   ', 'Tilbakekreving EF manuelle posteringer            ', 'EFVM    ', 2, 5, 'N', 'J', '   ', 'J', 'MEFT', 'OSR1302 ', '2013-05-03 12:42:06.812973'),
       ('MFORSK  ', 'Manuell postering FORSKUDD                        ', 'KORONA  ', 2, 5, 'N', 'J', '   ', 'J', 'MFOR', 'TOB1074B', '2020-04-06 12:57:13.257115'),
       ('MFP     ', 'Foreldrepenger, manuell                           ', 'KORTTID ', 1, 5, 'N', 'J', '   ', 'J', 'MKT1', 'OS16HL4 ', '2016-11-26 00:17:46.387405'),
       ('MFPREF  ', 'Foreldrepenger, manuell. AG refusjon              ', 'REFARBG ', 2, 10, 'N', 'J', '   ', 'J', 'MRA1', 'OS16HL4 ', '2016-11-26 00:17:58.127182'),
       ('MFRISINN', 'Kompensasjon inntektstap Frilanser og Næring      ', 'KORONA  ', 2, 10, 'N', 'J', '   ', 'J', 'MFRS', 'TOB1074C', '2020-05-04 08:04:02.688920'),
       ('MGHTB   ', 'Tilbakekreving GH manuelle posteringer            ', 'GH      ', 2, 5, 'N', 'J', '   ', 'J', 'MGHT', 'OSR1302 ', '2013-05-03 12:42:06.811769'),
       ('MGJENLEV', 'Gjenlevendepensjon tilleggsstønader               ', 'PEN     ', 2, 5, 'J', 'J', 'N  ', 'J', 'GJTS', 'TOB-3926', '2024-09-13 13:53:28.338794'),
       ('MHELSREF', 'Helserefusjoner NAV, manuell                      ', 'HELSEREF', 2, 32000, 'N', 'J', '   ', 'J', 'MHER', 'OS16HL4 ', '2016-11-26 00:22:04.237197'),
       ('MITGS   ', 'Gravferdstønad manuell postering                  ', 'GS      ', 2, 5, 'N', 'J', '   ', 'J', 'MGS ', 'POPPSETT', '2011-02-18 10:34:08.840841'),
       ('MIT05   ', 'Barnetrygd manuell postering                      ', 'BA      ', 2, 5, 'N', 'J', '   ', 'J', 'MBA ', 'POPPSETT', '2011-02-18 10:34:08.947180'),
       ('MIT18   ', 'Kontantstøtte manuell postering                   ', 'KS      ', 2, 5, 'N', 'J', '   ', 'J', 'MKS ', 'POPPSETT', '2011-02-18 10:34:08.947489'),
       ('MIT24   ', 'Menerstatning Yrkesskade - Manuell postering      ', 'YS      ', 2, 10, 'N', 'J', '   ', 'J', 'MYS ', 'POPPSETT', '2008-10-01 17:37:20.521263'),
       ('MIT26   ', 'Tidsbegrenset uførestønad, manuell                ', 'IT26    ', 2, 5, 'N', 'J', '   ', 'J', 'MUT ', 'OS16HL4 ', '2016-11-26 00:22:04.238423'),
       ('MIT27   ', 'Supplerende stønad manuell postering              ', 'IT27    ', 2, 5, 'N', 'J', '   ', 'J', 'MSU ', 'OSR1302 ', '2013-04-29 08:30:22.422283'),
       ('MIT28   ', 'Grunnstønad - Manuell postering                   ', 'GH      ', 2, 5, 'N', 'J', '   ', 'J', 'MGST', 'POPPSETT', '2008-10-01 17:37:20.521778'),
       ('MIT29   ', 'Hjelpestønad - Manuell postering                  ', 'GH      ', 2, 5, 'N', 'J', '   ', 'J', 'MHST', 'POPPSETT', '2008-10-01 17:37:20.522399'),
       ('MIT30BT ', 'Enslig forsørger - Barnetilsyn - Manuell          ', 'EFVM    ', 2, 2, 'N', 'J', '   ', 'J', 'MEBT', 'POPPSETT', '2009-10-29 13:08:28.468510'),
       ('MIT30FL ', 'Enslig forsørger - Tilskott til flytting - Manuell', 'EFVM    ', 2, 2, 'N', 'J', '   ', 'J', 'MEFL', 'POPPSETT', '2009-10-29 13:08:28.469176'),
       ('MIT30OG ', 'Enslig forsørger - Overgangsstønad - Manuell      ', 'PEN     ', 2, 4, 'N', 'J', '   ', 'J', 'MEOG', 'POPPSETT', '2009-10-29 13:08:28.467758'),
       ('MIT30UT ', 'Enslig forsørger - Utdanningsstønad - Manuell     ', 'EFVM    ', 2, 2, 'N', 'J', '   ', 'J', 'MEUT', 'POPPSETT', '2009-10-29 13:08:28.468941'),
       ('MIT31   ', 'Gjenlevendepensjon Virkemidler manuell postering  ', 'PEN     ', 2, 3, 'N', 'J', '   ', 'J', 'MGVM', 'POPPSETT', '2008-10-01 17:37:20.523580'),
       ('MIT32   ', 'Familiepleier Virkemidler manuell postering       ', 'PEN     ', 2, 2, 'N', 'J', '   ', 'J', 'MFVM', 'POPPSETT', '2008-10-01 17:37:20.524098'),
       ('MKS     ', 'Kontantstøtte, manuell postering                  ', 'KS      ', 2, 5, 'J', 'J', 'N  ', 'J', 'MKS ', 'TOB-2377', '2022-12-15 10:33:38.579229'),
       ('MKTALLE ', 'Kontoplan korttid                                 ', 'KTPOST  ', 2, 32000, 'N', 'J', '   ', 'J', '    ', 'OS16HL4 ', '2016-11-26 00:17:52.441846'),
       ('MKTBAL  ', 'Balansekonti korttid                              ', 'KTPOST  ', 2, 32000, 'N', 'J', '   ', 'J', '    ', 'OS16HL4 ', '2016-11-26 00:17:52.439284'),
       ('MKTTB   ', 'Tilbakekreving korttid                            ', 'KTPOST  ', 2, 5, 'N', 'J', '   ', 'J', '    ', 'OS16HL4 ', '2016-11-26 00:17:52.440612'),
       ('MOMSTILL', 'Omstillingsstønad                                 ', 'OMSTILL ', 2, 5, 'J', 'J', '   ', 'J', 'MOMS', 'TOB-2111', '2023-10-11 11:37:12.450734'),
       ('MOOP    ', 'Oms-, oppl.- og pleiepenger, manuell              ', 'KORTTID ', 2, 5, 'N', 'J', '   ', 'J', 'MKT1', 'OS16HL4 ', '2016-11-26 00:17:50.284777'),
       ('MOOPREF ', 'Oms-, oppl.- og pleiepenger man. AG refusjon      ', 'REFARBG ', 2, 15, 'N', 'J', '   ', 'J', 'MRA1', 'OS16HL4 ', '2016-11-26 00:17:50.285918'),
       ('MOSALLE ', 'Økonomi stønad alle                               ', 'SRPOST  ', 2, 32000, 'N', 'J', '   ', 'J', 'MMOS', 'POPPSETT', '2013-12-09 08:47:49.339574'),
       ('MPENAFP ', 'Avtalefestet pensjon manuell postering            ', 'PEN     ', 2, 5, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.399704'),
       ('MPENAFPP', 'Avtalefestet pensjon i privat sektor manuell post ', 'PEN     ', 2, 5, 'N', 'J', '   ', 'J', 'MAFP', 'POPPSETT', '2010-05-27 13:13:09.857856'),
       ('MPENALLE', 'Kontoplan pensjon                                 ', 'PENPOST ', 2, 32000, 'N', 'J', '   ', 'J', 'MPAL', 'POPPSETT', '2008-11-24 19:59:44.762627'),
       ('MPENAND ', 'Andre utbetalinger                                ', 'PEN     ', 2, 5, 'N', 'J', '   ', 'J', 'MPAN', 'POPPSETT', '2008-11-24 19:59:44.762990'),
       ('MPENAP  ', 'Alderspensjon manuell postering                   ', 'PEN     ', 2, 6, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.411490'),
       ('MPENBAL ', 'Balansekonti                                      ', 'PENPOST ', 2, 100, 'N', 'J', '   ', 'J', 'MPBA', 'POPPSETT', '2008-11-24 19:59:44.762432'),
       ('MPENBP  ', 'Barnepensjon manuell postering                    ', 'PEN     ', 2, 5, 'N', 'J', '   ', 'J', 'MPBP', 'POPPSETT', '2008-10-02 10:19:35.401358'),
       ('MPENDIV ', 'Diverse utbetalinger                              ', 'PEN     ', 2, 5, 'N', 'J', '   ', 'J', 'MPDI', 'POPPSETT', '2008-10-20 20:15:09.077826'),
       ('MPENFP  ', 'Tidligere familiepleierpensjon manuell postering  ', 'PEN     ', 2, 5, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.401776'),
       ('MPENGJ  ', 'Gjenlevendepensjon manuell postering              ', 'PEN     ', 2, 5, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.412550'),
       ('MPENGY  ', 'Gammel yrkesskadepensjon manuell postering        ', 'PEN     ', 2, 5, 'N', 'J', '   ', 'J', 'MPGY', 'POPPSETT', '2008-10-02 10:19:35.400768'),
       ('MPENKP  ', 'Krigspensjon manuell postering                    ', 'PEN     ', 2, 5, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.400235'),
       ('MPENNODU', 'Nødutbetaling                                     ', 'PEN     ', 2, 2, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2008-11-24 19:59:44.599964'),
       ('MPENOSLO', 'Oslo kommune tilleggspensjon manuell postering    ', 'PEN     ', 2, 5, 'N', 'J', 'OK ', 'J', 'MPOK', 'POPPSETT', '2008-10-02 10:19:35.398700'),
       ('MPENPTS ', 'Pensjonstrygden for sjømenn manuell postering     ', 'PEN     ', 2, 5, 'N', 'J', 'PTS', 'J', 'MPTS', 'POPPSETT', '2008-10-02 10:19:35.399310'),
       ('MPENSPK ', 'Statens pensjonskasse manuell postering           ', 'PEN     ', 2, 5, 'N', 'J', 'SPK', 'J', 'MSPK', 'POPPSETT', '2008-10-02 10:19:35.384348'),
       ('MPENTB  ', 'Tilbakekreving PEN manuell postering              ', 'PENPOST ', 2, 5, 'N', 'J', '   ', 'J', 'MPTB', 'OSR1302 ', '2013-04-29 08:30:22.351085'),
       ('MPENUP  ', 'Uførepensjon manuell postering                    ', 'PEN     ', 2, 5, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.411879'),
       ('MPERMIT ', 'Manuell postering PERMIT                          ', 'KORONA2 ', 2, 32000, 'N', 'J', '   ', 'J', 'MPER', 'TOB1074B', '2020-04-06 12:57:13.256801'),
       ('MREFUSJ ', 'Ref. helseutgifter, reise. Manuell postering      ', 'HELSEREF', 2, 5, 'N', 'J', '   ', 'J', '    ', 'IN784591', '2015-11-19 08:47:42.624834'),
       ('MREFUTG ', 'Refusjon utgiftsdekning, manuell                  ', 'REFUTG  ', 2, 5, 'N', 'J', '   ', 'J', 'MRU1', 'OS16HL4 ', '2016-11-26 00:17:57.601518'),
       ('MSKATT  ', 'Manuell opprydding skatt                          ', 'MSKATT  ', 2, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2010-06-02 10:12:39.406990'),
       ('MSP     ', 'Sykepenger, manuell                               ', 'KORTTID ', 1, 5, 'N', 'J', '   ', 'J', 'MKT1', 'OS16HL4 ', '2016-11-26 00:17:46.385836'),
       ('MSPREF  ', 'Sykepenger, manuell. AG refusjon                  ', 'REFARBG ', 2, 10, 'N', 'J', '   ', 'J', 'MRA1', 'OS16HL4 ', '2016-11-26 00:17:58.125862'),
       ('MSRALLE ', 'Kontoplan sentralisering regnskap                 ', 'SRPOST  ', 2, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2011-02-18 10:34:08.947800'),
       ('MSRBAL  ', 'Balansekonti sentralisering regnskap              ', 'SRPOST  ', 2, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2011-02-18 10:34:08.948045'),
       ('MSRTB   ', 'Tilbakekreving SR manuelle posteringer            ', 'SRPOST  ', 2, 5, 'N', 'J', '   ', 'J', 'MSRT', 'OSR1302 ', '2013-05-03 12:42:06.812334'),
       ('MSUALDER', 'Supplerende stønad alder, manuell                 ', 'IT27    ', 2, 5, 'J', 'J', 'N  ', 'J', 'MSU ', 'TOB-2293', '2023-01-25 14:28:42.468705'),
       ('MSUTB   ', 'Tilbakekreving SU manuelle posteringer            ', 'IT27    ', 2, 5, 'N', 'J', '   ', 'J', 'MSUT', 'OSR1302 ', '2013-05-03 12:42:06.738415'),
       ('MSUUFORE', 'Supplerende stønad uføre, manuell                 ', 'INNT    ', 2, 5, 'N', 'J', '   ', 'J', 'SMOT', 'TOB-1066', '2020-12-18 08:10:26.404136'),
       ('MSVP    ', 'Svangerskapspenger, manuell                       ', 'KORTTID ', 2, 5, 'N', 'J', '   ', 'J', 'MKT1', 'TOB-338 ', '2019-06-03 15:01:30.128934'),
       ('MSVPREF ', 'Svangerskapspenger, manuell. AG refusjon          ', 'REFARBG ', 2, 10, 'N', 'J', '   ', 'J', 'MRA1', 'TOB-338 ', '2019-06-03 15:01:30.128934'),
       ('MTBBTARE', 'Tilbakekreving ARBYT, manuelle posteringer        ', 'TBBT    ', 2, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.710108'),
       ('MTILLST ', 'Tilleggsstønad, manuell postering                 ', 'REFUTG  ', 2, 5, 'J', 'J', 'N  ', 'J', 'MTST', 'TOB-1958', '2023-03-11 09:33:46.582461'),
       ('MTILTPEN', 'Tiltakspenger, manuell postering                  ', 'REFUTG  ', 2, 5, 'J', 'J', 'N  ', 'J', 'MTP ', 'TOB-1958', '2023-03-11 09:33:46.583059'),
       ('MTPARENA', 'Tiltakspenger, manuell postering                  ', 'ARBYT   ', 2, 5, 'J', 'J', 'N  ', 'J', 'MTP ', 'TOB-1958', '2023-03-11 09:33:46.578181'),
       ('MTREKK  ', 'Manuell opprydding trekk                          ', 'KREDREF ', 2, 1000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2010-06-02 10:12:39.368829'),
       ('MTSTAREN', 'Tilleggstønad, manuell postering                  ', 'ARBYT   ', 2, 5, 'J', 'J', 'N  ', 'J', 'MTST', 'TOB-1958', '2023-03-11 09:33:46.525162'),
       ('MTUTB   ', 'Tilbakekreving TU manuelle posteringer            ', 'IT26    ', 2, 5, 'N', 'J', '   ', 'J', 'MTUT', 'OSR1302 ', '2013-05-03 12:42:06.812767'),
       ('MUFOREUT', 'Uføretrygd manuell postering                      ', 'INNT    ', 2, 5, 'N', 'J', '   ', 'J', 'MUFO', 'OSR1403 ', '2013-11-14 09:22:03.406001'),
       ('MUFORSPK', 'Uførepensjon fra SPK, manuell postering           ', 'INNT    ', 2, 5, 'N', 'J', '   ', 'J', 'MUSP', 'OSR1403 ', '2013-11-14 09:22:03.407424'),
       ('MUTALLE ', 'Uføretrygd manuell postering alle                 ', 'UTPOST  ', 2, 5, 'N', 'J', '   ', 'J', 'MUTA', 'OSR1403 ', '2013-10-31 17:15:48.787193'),
       ('MUTBAL  ', 'Uføretrygd manuell postering balanse              ', 'UTPOST  ', 2, 5, 'N', 'J', '   ', 'J', 'MUTB', 'OSR1403 ', '2013-10-31 17:15:48.786951'),
       ('MUTNODU ', 'Uføretrygd nødutbetaling                          ', 'INNT    ', 2, 5, 'N', 'J', '   ', 'J', 'MUFO', 'OSR1403 ', '2013-11-14 09:22:03.406370'),
       ('MUTTB   ', 'Tilbakekreving UT manuell                         ', 'UTPOST  ', 2, 5, 'N', 'J', '   ', 'J', 'MUTT', 'OSR1403 ', '2014-02-19 10:56:27.286989'),
       ('MYSTB   ', 'Tilbakekreving YS manuelle posteringer            ', 'YS      ', 2, 5, 'N', 'J', '   ', 'J', 'MYST', 'OSR1302 ', '2013-05-03 12:42:06.809747'),
       ('NEGKRED ', 'Negative trekkoppgjør                             ', 'NEGKRED ', 0, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2010-09-11 10:44:10.138421'),
       ('NEGSKATT', 'Negative skatteoppgjør                            ', 'NEGSKATT', 0, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2010-09-11 10:44:10.072732'),
       ('NODUARB ', 'Nødutbetaling,arbeidsytelser                      ', 'SRNODU  ', 2, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958K', '2023-03-11 09:34:14.536382'),
       ('NODUBA  ', 'Nødutbetaling barnetrygd                          ', 'SRNODU  ', 2, 5, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2011-02-18 10:34:08.948387'),
       ('NODUKORT', 'Nødutbetaling, korttid                            ', 'SRNODU  ', 2, 5, 'N', 'J', '   ', 'J', '    ', 'OS16HL4 ', '2016-11-26 00:22:08.274627'),
       ('NODUKS  ', 'Nødutbetaling kontantstøtte                       ', 'SRNODU  ', 2, 5, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2011-02-18 10:34:08.948758'),
       ('OM      ', 'Omsorgspenger                                     ', 'KORTTID ', 1, 2, 'J', 'J', '   ', 'J', 'MKT1', 'TOB-949 ', '2020-03-11 09:59:00.775887'),
       ('OMREF   ', 'Omsorgspenger AG Refusjon                         ', 'REFARBG ', 1, 10, 'J', 'J', '   ', 'J', 'MRA1', 'TOB-949 ', '2020-03-11 09:59:00.781206'),
       ('OMSTILL ', 'Omstillingsstønad                                 ', 'OMSTILL ', 1, 1, 'J', 'J', '   ', 'J', 'MOMS', 'TOB-2111', '2023-10-11 11:37:12.424524'),
       ('OOP     ', 'Oms-, oppl.- og pleiepenger                       ', 'KORTTID ', 1, 2, 'J', 'J', '   ', 'J', 'MKT1', 'OS16HL4 ', '2016-11-26 00:17:50.282291'),
       ('OOPREF  ', 'Oms-, oppl.- og pleiepenger AG refusjon           ', 'REFARBG ', 1, 10, 'J', 'J', '   ', 'J', 'MRA1', 'OS16HL4 ', '2016-11-26 00:17:50.283602'),
       ('OPP     ', 'Opplæringspenger                                  ', 'KORTTID ', 1, 2, 'J', 'J', '   ', 'J', 'MKT1', 'TOB-949 ', '2020-03-11 09:59:00.754344'),
       ('OPPREF  ', 'Opplæringspenger AG refusjon                      ', 'REFARBG ', 1, 10, 'J', 'J', '   ', 'J', 'MRA1', 'TOB-949 ', '2020-03-11 09:59:00.779950'),
       ('PB      ', 'Pleiepenger barn                                  ', 'KORTTID ', 1, 5, 'J', 'J', '   ', 'J', 'MKT1', 'TOB-949 ', '2020-03-11 09:59:00.777148'),
       ('PBREF   ', 'Pleiepenger barn AG refusjon                      ', 'REFARBG ', 1, 10, 'J', 'J', '   ', 'J', 'MRA1', 'TOB-949 ', '2020-03-11 09:59:00.782681'),
       ('PENAFP  ', 'Avtalefestet pensjon                              ', 'PEN     ', 1, 1, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.408933'),
       ('PENAFPP ', 'Avtalefestet pensjon i privat sektor              ', 'PEN     ', 1, 1, 'N', 'J', '   ', 'J', 'MAFP', 'POPPSETT', '2010-05-27 13:13:09.897044'),
       ('PENAP   ', 'Alderspensjon                                     ', 'PEN     ', 1, 1, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.402188'),
       ('PENBP   ', 'Barnepensjon                                      ', 'PEN     ', 1, 1, 'N', 'J', '   ', 'J', 'MPBP', 'POPPSETT', '2008-10-02 10:19:35.407027'),
       ('PENFP   ', 'Tidligere familiepleierpensjon                    ', 'PEN     ', 1, 1, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.406386'),
       ('PENGJ   ', 'Gjenlevendepensjon                                ', 'PEN     ', 1, 1, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.405584'),
       ('PENGY   ', 'Gammel yrkesskadepensjon                          ', 'PEN     ', 1, 1, 'N', 'J', '   ', 'J', 'MPGY', 'POPPSETT', '2008-10-02 10:19:35.407755'),
       ('PENKP   ', 'Krigspensjon                                      ', 'PEN     ', 1, 2, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.408496'),
       ('PENOSLO ', 'Oslo kommune tilleggspensjon                      ', 'PEN     ', 1, 1, 'N', 'J', 'OK ', 'J', 'MPOK', 'POPPSETT', '2008-10-02 10:19:35.410252'),
       ('PENPTS  ', 'Pensjonstrygden for sjømenn                       ', 'PEN     ', 1, 1, 'N', 'J', 'PTS', 'J', 'MPTS', 'POPPSETT', '2008-10-02 10:19:35.409715'),
       ('PENSPK  ', 'Statens pensjonskasse                             ', 'PEN     ', 1, 1, 'N', 'J', 'SPK', 'J', 'MSPK', 'POPPSETT', '2008-10-02 10:19:35.410995'),
       ('PENUP   ', 'Uførepensjon                                      ', 'PEN     ', 1, 2, 'N', 'J', '   ', 'J', 'MPEN', 'POPPSETT', '2008-10-02 10:19:35.404893'),
       ('PERMIT  ', 'Permitteringspenger, korona                       ', 'KORONA2 ', 1, 20, 'J', 'J', '   ', 'J', 'MPER', 'TOB-1074', '2020-03-31 08:18:54.006553'),
       ('PN      ', 'Pleiepenger nærstående                            ', 'KORTTID ', 1, 2, 'J', 'J', '   ', 'J', 'MKT1', 'TOB-949 ', '2020-03-11 09:59:00.778594'),
       ('PNREF   ', 'Pleiepenger nærstående AG refusjon                ', 'REFARBG ', 1, 10, 'J', 'J', '   ', 'J', 'MKT1', 'TOB-949 ', '2020-03-11 09:59:00.783945'),
       ('PREDATOR', 'Interne trekk til TI                              ', 'PREDATOR', 0, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2006-01-09 11:34:54.545571'),
       ('REFUTG  ', 'Refusjon utgiftsdekning                           ', 'REFUTG  ', 1, 1, 'J', 'J', '   ', 'J', 'MRU1', 'OS16HL4 ', '2016-11-26 00:17:57.582286'),
       ('SAPO01  ', 'Behandleroppgjør                                  ', 'SAPO01  ', 1, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2005-10-22 15:43:11.664751'),
       ('SKATOPPG', 'Skatteoppgjør                                     ', 'SKATOPPG', 0, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2006-01-09 11:34:54.544595'),
       ('SP      ', 'Sykepenger                                        ', 'KORTTID ', 1, 10, 'J', 'J', '   ', 'J', 'MKT1', 'OS16HL4 ', '2016-11-26 00:17:58.119048'),
       ('SPREF   ', 'Sykepenger, AG refusjon                           ', 'REFARBG ', 1, 12, 'J', 'J', '   ', 'J', 'MRA1', 'OS16HL4 ', '2016-11-26 00:17:58.120755'),
       ('SUALDER ', 'Supplerende stønad alder                          ', 'IT27    ', 1, 1, 'J', 'J', 'N  ', 'J', 'MSU ', 'TOB-2293', '2023-01-25 14:28:42.466836'),
       ('SUUFORE ', 'Supplerende stønad uføre                          ', 'INNT    ', 1, 1, 'J', 'J', '   ', 'J', 'SMOT', 'TOB-1066', '2020-12-18 08:10:26.362715'),
       ('SVALSKAT', 'Svalbardskatt                                     ', 'SVALSKAT', 0, 32000, 'N', 'J', '   ', 'J', '    ', 'POPPSETT', '2006-01-09 11:34:54.391381'),
       ('SPKBP   ', 'Statens pensjonskasse, barnepensjon               ', 'PEN     ', 1, 1, 'J', 'J', 'SPK', 'J', 'MSPK', 'TOB-4381', '2024-12-12 09:02:08.285748'),
       ('SVP     ', 'Svangerskapspenger                                ', 'KORTTID ', 1, 4, 'J', 'J', '   ', 'J', 'MKT1', 'TOB-338 ', '2019-06-03 15:01:30.128934'),
       ('SVPREF  ', 'Svangerskapspenger, AG refusjon                   ', 'REFARBG ', 1, 10, 'J', 'J', '   ', 'J', 'MRA1', 'TOB-338 ', '2019-06-03 15:01:30.128934'),
       ('TBAAP   ', 'AAP ikke innkreving                               ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.700698'),
       ('TBAAPKR ', 'AAP til innkreving                                ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.704307'),
       ('TBAFP   ', 'AFP ikke innkreving                               ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.509959'),
       ('TBAFPKR ', 'AFP til innkreving                                ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.593837'),
       ('TBAFPP  ', 'AFP Privat ikke innkreving                        ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.593547'),
       ('TBAFPPKR', 'AFP Privat til innkreving                         ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.596493'),
       ('TBAG    ', 'AGPERM ikke innkreving                            ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1602', '2021-05-27 11:40:46.518937'),
       ('TBAGKR  ', 'AGPERM til innkreving                             ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1602', '2021-05-27 11:40:46.522017'),
       ('TBAP    ', 'Alder ikke innkreving                             ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.591210'),
       ('TBAPKR  ', 'Alder til innkreving                              ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.594057'),
       ('TBARENA ', 'AAP ikke innkreving                               ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.697657'),
       ('TBARENKR', 'AAP til innkreving                                ', 'TB      ', 1, 32000, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.703650'),
       ('TBBA    ', 'BA ikke innkreving                                ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1558', '2021-09-14 11:02:43.403291'),
       ('TBBAKR  ', 'BA til innkreving                                 ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1558', '2021-09-14 11:02:43.427389'),
       ('TBBP    ', 'Barnepensjon ikke innkreving                      ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.591593'),
       ('TBBPKR  ', 'Barnepensjon til innkreving                       ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.594349'),
       ('TBBTAAP ', 'Nedbetalt AAP                                     ', 'TBBT    ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.707650'),
       ('TBBTAFP ', 'Nedbetalt Avtalefestet Pensjon                    ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:23.991480'),
       ('TBBTAFPP', 'Nedbetalt Avtalefestet Pensjon i privat           ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.014984'),
       ('TBBTAGPE', 'Nedbetalt Lønnskomp. arbeidsgiver                 ', 'TBBT    ', 1, 10, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.032316'),
       ('TBBTAP  ', 'Nedbetalt Alderspensjon                           ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.016503'),
       ('TBBTARE ', 'Nedbetalt Arenaytelser                            ', 'TBBT    ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.707021'),
       ('TBBTBA  ', 'Nedbetalt barnetrygd                              ', 'TBBT    ', 1, 2, 'J', 'N', '   ', 'J', '    ', 'TOB-1558', '2021-09-14 11:02:44.215723'),
       ('TBBTBP  ', 'Nedbetalt Barnepensjon                            ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.017396'),
       ('TBBTDP  ', 'Nedbetalt DP                                      ', 'TBBT    ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.708313'),
       ('TBBTEFBT', 'Nedbetalt enslig forsørger barnetilsyn            ', 'TBBT    ', 1, 2, 'J', 'N', '   ', 'J', '    ', 'TOB-1754', '2021-12-09 14:32:49.471664'),
       ('TBBTEFOG', 'Nedbetalt enslig forsørger overgangsstønad        ', 'TBBT    ', 1, 2, 'J', 'N', '   ', 'J', '    ', 'TOB-1754', '2021-12-09 14:32:47.313391'),
       ('TBBTEFSP', 'Nedbetalt enslig forsørger skolepenger            ', 'TBBT    ', 1, 2, 'J', 'N', '   ', 'J', '    ', 'TOB-3528', '2024-08-16 11:44:24.026187'),
       ('TBBTFOP ', 'Nedbetalt Foreldrepenger                          ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.028935'),
       ('TBBTFP  ', 'Nedbetalt Tidligere familiepleierpensjon          ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.020376'),
       ('TBBTFRIS', 'Nedbetalt Komp. inntektstap Frilanser og Næring   ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.027414'),
       ('TBBTGJ  ', 'Nedbetalt Gjenlevendepensjon                      ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.021691'),
       ('TBBTGJLV', 'Nedbetalt gjenlevende pensjon                     ', 'TBBT    ', 1, 5, 'J', 'N', 'N  ', 'J', '    ', 'TOB-3926', '2024-09-13 13:53:28.351451'),
       ('TBBTGY  ', 'Nedbetalt Gammel yrkeskadepensjon                 ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.023088'),
       ('TBBTKP  ', 'Nedbetalt Krigspensjon                            ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.024911'),
       ('TBBTKS  ', 'Nedbetalt kontantstøtte                           ', 'TBBT    ', 1, 2, 'J', 'N', 'N  ', 'J', '    ', 'TOB-2377', '2022-12-15 10:33:38.580065'),
       ('TBBTLKPE', 'Nedbetalt lønnskompensasjon, PERMIT               ', 'TBBT    ', 1, 10, 'J', 'N', '   ', 'J', '    ', 'TOB-1602', '2021-05-27 11:40:46.525802'),
       ('TBBTOM  ', 'Nedbetalt Omsorgspenger                           ', 'TBBT    ', 1, 2, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.257371'),
       ('TBBTOMST', 'Nedbetalt omstillingsstønad                       ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-2111', '2023-10-11 11:37:13.229447'),
       ('TBBTOPP ', 'Nedbetalt Opplæringspenger                        ', 'TBBT    ', 1, 2, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.238498'),
       ('TBBTPB  ', 'Nedbetalt pleipenger                              ', 'TBBT    ', 1, 2, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.210073'),
       ('TBBTPN  ', 'Nedbetalt pleiepenger nærstående                  ', 'TBBT    ', 1, 2, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.225406'),
       ('TBBTREFU', 'Nedbetalt Refusjon utgiftsdekning                 ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.031271'),
       ('TBBTSUAL', 'Nedbetalt Supplerende stønad alder                ', 'TBBT    ', 1, 2, 'J', 'N', 'N  ', 'J', '    ', 'TOB-2293', '2023-01-25 14:28:42.473644'),
       ('TBBTSUUF', 'Nedbetalt Supplerende stønad Uføre                ', 'TBBT    ', 1, 2, 'J', 'N', '   ', 'J', '    ', 'TOB-1964', '2022-04-06 14:32:47.347016'),
       ('TBBTSVP ', 'Nedbetalt Svangerskapspenger                      ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.030130'),
       ('TBBTTP  ', 'Nedbetalt tiltakspenger                           ', 'TBBT    ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.709561'),
       ('TBBTTST ', 'Nedbetalt Tilleggsstønad                          ', 'TBBT    ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.708951'),
       ('TBBTUP  ', 'Nedbetalt Uførepensjon                            ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'TOB-1281', '2021-09-15 16:02:24.026073'),
       ('TBBTUT  ', 'Nedbetalt uføretrygd                              ', 'TBBT    ', 1, 2, 'N', 'J', '   ', 'J', '    ', 'OS16HL4 ', '2016-11-26 00:22:06.262352'),
       ('TBDP    ', 'DP ikke innkreving                                ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.701264'),
       ('TBDPAR  ', 'DP ikke innkreving                                ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.698968'),
       ('TBDPKR  ', 'DP til innkreving                                 ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.705057'),
       ('TBEFBT  ', 'Enslig forsørger barnetilsyn ikke innkreving      ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1754', '2021-12-09 14:32:47.303451'),
       ('TBEFBTKR', 'Enslig forsørger barnetilsyn innkreving           ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1754', '2021-12-09 14:32:47.309681'),
       ('TBEFOG  ', 'Enslig forsørger overgangsstønad ikke innkreving  ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1754', '2021-12-09 14:32:47.273084'),
       ('TBEFOGKR', 'Enslig forsørger overgangsstønad innkreving       ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1754', '2021-12-09 14:32:47.307946'),
       ('TBEFSP  ', 'Enslig forsørger skolepenger ikke innkreving      ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1754', '2021-12-09 14:32:47.305516'),
       ('TBEFSPKR', 'Enslig forsørger skolepenger innkreving           ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1754', '2021-12-09 14:32:47.311436'),
       ('TBES    ', 'Engangstønad ikke innkreving                      ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-751 ', '2019-10-19 20:03:54.866848'),
       ('TBESKR  ', 'Engangstønad til innkreving                       ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-751 ', '2019-10-19 20:03:54.802002'),
       ('TBFO    ', 'FORSKUDD ikke innkreving                          ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1582', '2021-05-27 11:40:44.579901'),
       ('TBFOKR  ', 'FORSKUDD til innkreving                           ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1582', '2021-05-27 11:40:44.578097'),
       ('TBFORP  ', 'Foreldrepenger ikke innkreving                    ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-533 ', '2019-09-13 22:14:43.171992'),
       ('TBFORPKR', 'Foreldrepenger til innkreving                     ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-533 ', '2019-09-13 22:14:43.205359'),
       ('TBFP    ', 'Familiepleier ikke innkreving                     ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.591869'),
       ('TBFPKR  ', 'Familiepleier til innkreving                      ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.594999'),
       ('TBFRIS  ', 'FRISINN ikke innkreving                           ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1163', '2020-05-29 09:54:11.976488'),
       ('TBFRISKR', 'FRISINN til innkreving                            ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1163', '2020-05-29 09:54:12.034969'),
       ('TBGJ    ', 'Gjenlevende ikke innkreving                       ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.592313'),
       ('TBGJKR  ', 'Gjenlevende til innkreving                        ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.595324'),
       ('TBGJLV  ', 'Gjenlevende pensjon ikke innkreving               ', 'TB      ', 1, 5, 'J', 'N', 'N  ', 'J', '    ', 'TOB-3926', '2024-09-13 13:53:28.341126'),
       ('TBGJLVKR', 'Gjenlevende pensjon til innkreving                ', 'TB      ', 1, 5, 'J', 'N', 'N  ', 'J', '    ', 'TOB-3926', '2024-09-13 13:53:28.349094'),
       ('TBGY    ', 'Gammel Yrkesskade ikke innkreving                 ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.592632'),
       ('TBGYKR  ', 'Gammel Yrkesskade til innkreving                  ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.595627'),
       ('TBKP    ', 'Krigspensjon ikke innkreving                      ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.593080'),
       ('TBKPKR  ', 'Krigspensjon til innkreving                       ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.595969'),
       ('TBKRAV  ', 'Tilbakekrevingskrav                               ', 'TBKRAV  ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.596722'),
       ('TBKS    ', 'KS ikke innkreving                                ', 'TB      ', 1, 5, 'J', 'N', 'N  ', 'J', '    ', 'TOB-2377', '2022-12-15 10:33:38.581015'),
       ('TBKSKR  ', 'KS til innkreving                                 ', 'TB      ', 1, 32000, 'J', 'N', 'N  ', 'J', '    ', 'TOB-2377', '2022-12-15 10:33:38.582200'),
       ('TBOM    ', 'OM ikke innkreving                                ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.104088'),
       ('TBOMKR  ', 'OM til innkreving                                 ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.170895'),
       ('TBOMST  ', 'Omstillingsstønad ikke innkreving                 ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-2111', '2023-10-11 11:37:13.226423'),
       ('TBOMSTKR', 'Omstillingsstønad til innkreving                  ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-2111', '2023-10-11 11:37:13.228187'),
       ('TBOOP   ', 'OOP ikke innkreving                               ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-533 ', '2019-09-13 22:14:43.201670'),
       ('TBOOPKR ', 'OOP til innkreving                                ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-533 ', '2019-09-13 22:14:43.207184'),
       ('TBOPP   ', 'OPP ikke innkreving                               ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.069942'),
       ('TBOPPKR ', 'OPP til innkreving                                ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.157135'),
       ('TBPB    ', 'PB ikke innkreving                                ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.122120'),
       ('TBPBKR  ', 'PB til innkreving                                 ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.182354'),
       ('TBPE    ', 'PERMIT ikke innkreving                            ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1602', '2021-05-27 11:40:46.512670'),
       ('TBPEKR  ', 'PERMIT til innkreving                             ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1602', '2021-05-27 11:40:46.516115'),
       ('TBPN    ', 'PN ikke innkreving                                ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.141792'),
       ('TBPNKR  ', 'PN til innkreving                                 ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1295', '2021-10-27 10:09:51.196853'),
       ('TBSP    ', 'Sykepenger ikke innkreving                        ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-533 ', '2019-09-13 22:14:43.203525'),
       ('TBSPKR  ', 'Sykepenger til innkreving                         ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-533 ', '2019-09-13 22:14:43.209102'),
       ('TBSUAL  ', 'Supplerende stønad alder, innkreving              ', 'TB      ', 1, 32000, 'J', 'N', 'N  ', 'J', '    ', 'TOB-2293', '2023-01-25 14:28:42.469990'),
       ('TBSUALKR', 'Supplerende stønad alder, ikke innkreving         ', 'TB      ', 1, 5, 'J', 'N', 'N  ', 'J', '    ', 'TOB-2293', '2023-01-25 14:28:42.471968'),
       ('TBSUUFKR', 'Supplerende stønad Uføre, innkreving              ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-1964', '2022-04-06 14:32:47.344659'),
       ('TBSUUFOR', 'Supplerende stønad Uføre, ikke innkreving         ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-1964', '2022-04-06 14:32:47.318916'),
       ('TBSVP   ', 'Svangerskapspenger ikke innkreving                ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'TOB-751 ', '2019-10-19 20:03:54.800873'),
       ('TBSVPKR ', 'Svangerskapspenger til innkreving                 ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'TOB-751 ', '2019-10-19 20:03:54.774021'),
       ('TBTILLKR', 'Tilleggsstønad til innkreving                     ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.705789'),
       ('TBTILLST', 'Tilleggstønad ikke innkreving                     ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.701894'),
       ('TBTILTKR', 'Tiltakspenger til innkreving                      ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.706376'),
       ('TBTILTP ', 'Tiltakspenger ikke innkreving                     ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.702971'),
       ('TBTPAR  ', 'Tiltakspenger ikke innkreving                     ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.700146'),
       ('TBTSTAR ', 'Tilleggstønad ikke innkreving                     ', 'TB      ', 1, 5, 'J', 'J', 'N  ', 'J', '    ', 'TOB1958F', '2023-03-11 09:34:06.699602'),
       ('TBUP    ', 'Uførepensjon ikke innkreving                      ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.593334'),
       ('TBUPKR  ', 'Uførepensjon til innkreving                       ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'POPPSETT', '2011-01-18 15:41:53.596193'),
       ('TBUT    ', 'Uføretrygd ikke innkreving                        ', 'TB      ', 1, 5, 'J', 'N', '   ', 'J', '    ', 'OSR1403 ', '2013-10-31 17:15:48.787567'),
       ('TBUTKR  ', 'Uføretrygd til innkreving                         ', 'TB      ', 1, 32000, 'J', 'N', '   ', 'J', '    ', 'OSR1403 ', '2013-10-31 17:15:48.787741'),
       ('TILLST  ', 'Tilleggsstønad                                    ', 'ARBTIL  ', 1, 99, 'J', 'J', 'N  ', 'J', 'MTST', 'TOB-4247', '2023-03-11 09:33:46.580091'),
       ('TILTPENG', 'Tiltakspenger                                     ', 'ARBTIL  ', 1, 99, 'J', 'J', 'N  ', 'J', 'MTP ', 'TOB-4247', '2023-03-11 09:33:46.580696'),
       ('TPARENA ', 'Tiltakspenger                                     ', 'ARBYT   ', 1, 99, 'J', 'J', 'N  ', 'J', 'MTP ', 'TOB-1958', '2023-03-11 09:33:46.522828'),
       ('TSTARENA', 'Tilleggsstønad                                    ', 'ARBYT   ', 1, 99, 'J', 'J', 'N  ', 'J', 'MTST', 'TOB-1958', '2023-03-11 09:33:46.522037'),
       ('UFORENG ', 'Uføretrygdede engangsutbetaling                   ', 'INNT    ', 1, 1, 'J', 'J', '   ', 'J', '    ', 'TOB-2604', '2023-01-26 13:42:04.425446'),
       ('UFORESPK', 'Uførepensjon fra SPK                              ', 'INNT    ', 1, 1, 'N', 'J', 'SPK', 'J', 'MUSP', 'OSR1403 ', '2013-11-14 09:22:03.407138'),
       ('UFOREUT ', 'Uføretrygd                                        ', 'INNT    ', 1, 1, 'N', 'J', '   ', 'J', 'MUFO', 'OSR1403 ', '2013-11-14 09:22:03.385426'),
       ('UNG     ', 'Ungdomsytelse                                     ', 'UNG     ', 1, 1, 'J', 'J', '   ', 'J', 'UNG ', 'TOB-4122', '2024-09-27 09:58:44.819956'),
       ('UTBINNKR', 'Utbetaling for mye innkrevd                       ', 'UTBINNKR', 1, 1, 'N', 'J', '   ', 'J', '    ', 'OPOS592 ', '2013-08-21 16:17:48.552935');

DROP TABLE IF EXISTS T_FAGOMR_REGEL;
CREATE TABLE T_FAGOMR_REGEL
(
    KODE_FAGOMRAADE CHAR(8)                             not null,
    KODE_REGEL      CHAR(8)                             not null,
    DATO_FOM        DATE                                not null,
    BRUKERID        CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG     TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

DROP TABLE IF EXISTS T_FAGOMR_KORRARSAK;
CREATE TABLE T_FAGOMR_KORRARSAK
(
    KODE_FAGOMRAADE  CHAR(8)                             not null,
    KODE_AARSAK_KORR CHAR(4)                             not null,
    MEDFORER_KORR    CHAR(1)                             not null,
    NY_ATTESTASJON   CHAR(1)                             not null,
    BRUKERID         CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG      TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

DROP TABLE IF EXISTS T_FAGO_BILAGSTYPE;
CREATE TABLE T_FAGO_BILAGSTYPE
(
    KODE_FAGOMRAADE  CHAR(8)                             not null,
    TYPE_BILAG       CHAR(4)                             not null,
    DATO_FOM         DATE                                not null,
    DATO_TOM         DATE,
    AUTO_FAGSYSTEMID CHAR(1)                             not null,
    BRUKERID         CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG      TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

DROP TABLE IF EXISTS T_KORR_AARSAK;

CREATE TABLE T_KORR_AARSAK
(
    KODE_AARSAK_KORR CHAR(4)                             NOT NULL,
    BESKRIVELSE      CHAR(50)                            NOT NULL,
    BRUKERID         CHAR(8)      DEFAULT 'CURRENT USER' NOT NULL,
    TIDSPKT_REG      TIMESTAMP(6) DEFAULT CURRENT TIMESTAMP NOT NULL
);


DROP TABLE IF EXISTS T_FAGO_KLASSEKODE;
CREATE TABLE T_FAGO_KLASSEKODE
(
    KODE_FAGOMRAADE CHAR(8)                             not null,
    KODE_KLASSE     CHAR(50)                            not null,
    BRUKERID        CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG     TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

DROP TABLE IF EXISTS T_ENHETSTYPE;
CREATE TABLE T_ENHETSTYPE
(
    TYPE_ENHET  CHAR(4)                             not null,
    BESKRIVELSE CHAR(50)                            not null,
    BRUKERID    CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

insert into T_ENHETSTYPE (TYPE_ENHET, BESKRIVELSE, BRUKERID, TIDSPKT_REG)
values ('ANKE', 'Ankeenhet                                         ', 'POPPSETT', '0001-01-01 00:00:00.000000'),
       ('BEH ', 'Behandlende trygdekontor                          ', 'POPPSETT', '0001-01-01 00:00:00.000000'),
       ('BOS ', 'Bostedstrygdekontor                               ', 'POPPSETT', '0001-01-01 00:00:00.000000');


DROP TABLE IF EXISTS T_OPPDRAG_STATUS;
CREATE TABLE T_OPPDRAG_STATUS
(
    OPPDRAGS_ID INTEGER                             not null,
    KODE_STATUS CHAR(4)                             not null,
    LOPENR      SMALLINT                            not null,
    TIDSPKT_REG TIMESTAMP(6) default CURRENT TIMESTAMP not null,
    BRUKERID    CHAR(8)      default 'CURRENT USER' not null
);

DROP TABLE IF EXISTS T_OPPDRAGSENHET;
CREATE TABLE T_OPPDRAGSENHET
(
    OPPDRAGS_ID INTEGER      not null,
    TYPE_ENHET  CHAR(4)      not null,
    DATO_FOM    DATE,
    NOKKEL_ID   SMALLINT     not null,
    ENHET       CHAR(13)     not null,
    TIDSPKT_REG TIMESTAMP(6) not null,
    BRUKERID    CHAR(8)      not null
);

DROP TABLE IF EXISTS T_LINJE_STATUS;
CREATE TABLE T_LINJE_STATUS
(
    OPPDRAGS_ID INTEGER                             not null,
    LINJE_ID    SMALLINT                            not null,
    KODE_STATUS CHAR(4)                             not null,
    DATO_FOM    DATE,
    LOPENR      SMALLINT                            not null,
    TIDSPKT_REG TIMESTAMP(6) default CURRENT TIMESTAMP not null,
    BRUKERID    CHAR(8)      default 'CURRENT USER' not null
);

DROP TABLE IF EXISTS T_KORREKSJON;
CREATE TABLE T_KORREKSJON
(
    OPPDRAGS_ID      INTEGER                 not null,
    LINJE_ID         SMALLINT                not null,
    OPPDRAGS_ID_KORR INTEGER                 not null,
    LINJE_ID_KORR    INTEGER                 not null,
    TIDSPKT_REG      TIMESTAMP(6) default CURRENT TIMESTAMP not null,
    BRUKERID         CHAR(8)      default '' not null
);

DROP TABLE IF EXISTS T_LINJE_VEDTAKSSATS;
CREATE TABLE T_LINJE_VEDTAKSSATS
(
    OPPDRAGS_ID INTEGER                             not null,
    LINJE_ID    SMALLINT                            not null,
    VEDTAKSSATS DECIMAL(11, 2)                      not null,
    BRUKERID    CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

DROP TABLE IF EXISTS T_KJOREDATO;
CREATE TABLE T_KJOREDATO
(
    KJOREDATO DATE not null
);

DROP TABLE IF EXISTS T_VALUTA;
CREATE TABLE T_VALUTA
(
    OPPDRAGS_ID INTEGER                             not null,
    LINJE_ID    SMALLINT                            not null,
    TYPE_VALUTA CHAR(4)                             not null,
    DATO_FOM    DATE                                not null,
    NOKKEL_ID   SMALLINT                            not null,
    VALUTA      CHAR(3)                             not null,
    FEILREG     CHAR(1)                             not null,
    TIDSPKT_REG TIMESTAMP(6) default CURRENT TIMESTAMP not null,
    BRUKERID    CHAR(8)      default 'CURRENT USER' not null
);

DROP TABLE IF EXISTS T_KRAVHAVER;
CREATE TABLE T_KRAVHAVER
(
    OPPDRAGS_ID  INTEGER                             not null,
    LINJE_ID     SMALLINT                            not null,
    KRAVHAVER_ID CHAR(11)                            not null,
    DATO_FOM     DATE                                not null,
    TIDSPKT_REG  TIMESTAMP(6) default CURRENT TIMESTAMP not null,
    BRUKERID     CHAR(8)      default 'CURRENT USER' not null
);

DROP TABLE IF EXISTS T_SKYLDNER;
CREATE TABLE T_SKYLDNER
(
    OPPDRAGS_ID INTEGER      not null,
    LINJE_ID    SMALLINT     not null,
    SKYLDNER_ID CHAR(11)     not null,
    DATO_FOM    DATE,
    TIDSPKT_REG TIMESTAMP(6) not null,
    BRUKERID    CHAR(8)      not null
);

DROP TABLE IF EXISTS T_LINJEENHET;
CREATE TABLE T_LINJEENHET
(
    OPPDRAGS_ID INTEGER      not null,
    LINJE_ID    SMALLINT     not null,
    TYPE_ENHET  CHAR(4)      not null,
    DATO_FOM    DATE,
    NOKKEL_ID   SMALLINT     not null,
    ENHET       CHAR(13)     not null,
    TIDSPKT_REG TIMESTAMP(6) not null,
    BRUKERID    CHAR(8)      not null
);

DROP TABLE IF EXISTS T_GRAD;
CREATE TABLE T_GRAD
(
    OPPDRAGS_ID INTEGER      not null,
    LINJE_ID    SMALLINT     not null,
    TYPE_GRAD   CHAR(4)      not null,
    GRAD        SMALLINT     not null,
    TIDSPKT_REG TIMESTAMP(6) not null,
    BRUKERID    CHAR(8)      not null
);

DROP TABLE IF EXISTS T_KID;
CREATE TABLE T_KID
(
    OPPDRAGS_ID INTEGER      not null,
    LINJE_ID    SMALLINT     not null,
    KID         CHAR(26)     not null,
    DATO_FOM    DATE,
    TIDSPKT_REG TIMESTAMP(6) not null,
    BRUKERID    CHAR(8)      not null
);

DROP TABLE IF EXISTS T_MAKS_DATO;
CREATE TABLE T_MAKS_DATO
(
    OPPDRAGS_ID INTEGER      not null,
    LINJE_ID    SMALLINT     not null,
    MAKS_DATO   DATE,
    DATO_FOM    DATE,
    TIDSPKT_REG TIMESTAMP(6) not null,
    BRUKERID    CHAR(8)      not null
);

DROP TABLE IF EXISTS T_TEKST;
CREATE TABLE T_TEKST
(
    OPPDRAGS_ID INTEGER      not null,
    LINJE_ID    SMALLINT     not null,
    TEKST_LNR   SMALLINT     not null,
    NOKKEL_ID   SMALLINT     not null,
    TEKSTKODE   CHAR(4)      not null,
    TEKST       CHAR(40)     not null,
    DATO_FOM    DATE,
    DATO_TOM    DATE,
    FEILREG     CHAR(1)      not null,
    TIDSPKT_REG TIMESTAMP(6) not null,
    BRUKERID    CHAR(8)      not null
);

DROP TABLE IF EXISTS T_OPPDRAG;
CREATE TABLE T_OPPDRAG
(
    OPPDRAGS_ID        INTEGER                             not null,
    FAGSYSTEM_ID       CHAR(30)                            not null,
    KODE_FAGOMRAADE    CHAR(8)                             not null,
    FREKVENS           CHAR(4)                             not null,
    KJOR_IDAG          CHAR(1)                             not null,
    STONAD_ID          CHAR(10)                            not null,
    DATO_FORFALL       DATE,
    OPPDRAG_GJELDER_ID CHAR(11)                            not null,
    TYPE_BILAG         CHAR(4)                             not null,
    BRUKERID           CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG        TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

DROP TABLE IF EXISTS T_OPPDRAGSLINJE;
CREATE TABLE T_OPPDRAGSLINJE
(
    OPPDRAGS_ID        INTEGER        not null,
    LINJE_ID           SMALLINT       not null,
    DELYTELSE_ID       CHAR(30)       not null,
    SATS               DECIMAL(11, 2) not null,
    TYPE_SATS          CHAR(4)        not null,
    KJOR_IDAG          CHAR(1)        not null,
    DATO_VEDTAK_FOM    DATE,
    DATO_VEDTAK_TOM    DATE,
    ATTESTERT          CHAR(1)        not null,
    KODE_ARBGIVER      CHAR(1)        not null,
    VEDTAK_ID          CHAR(10)       not null,
    HENVISNING         CHAR(30)       not null,
    UTBETALES_TIL_ID   CHAR(11)       not null,
    KODE_KLASSE        CHAR(50)       not null,
    SKYLDNER_ID        CHAR(11)       not null,
    KID                CHAR(24)       not null,
    KRAVHAVER_ID       CHAR(11)       not null,
    DATO_KRAVHAVER_FOM DATE,
    TYPE_SOKNAD        CHAR(10)       not null,
    MAKSDATO           DATE,
    REFUNDERES_ID      CHAR(11),
    TIDSPKT_REG        TIMESTAMP(6)   not null,
    BRUKERID           CHAR(8)        not null
);

DROP TABLE IF EXISTS T_OMPOSTERING;
CREATE TABLE T_OMPOSTERING
(
    GJELDER_ID        CHAR(11)                            not null,
    KODE_FAGGRUPPE    CHAR(8)                             not null,
    LOPENR            SMALLINT                            not null,
    OMPOSTERING       CHAR(1)                             not null,
    DATO_OMPOSTER_FOM DATE,
    FEILREG           CHAR(1)                             not null,
    BEREGNINGS_ID     INTEGER,
    UTFORT            CHAR(1)                             not null,
    BRUKERID          CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG       TIMESTAMP(6) default CURRENT TIMESTAMP not null,
    KRAVGRUNNLAG_ID   INTEGER
);

DROP TABLE IF EXISTS T_ATTESTASJON;
CREATE TABLE T_ATTESTASJON
(
    OPPDRAGS_ID      INTEGER                             not null,
    LINJE_ID         SMALLINT                            not null,
    ATTESTANT_ID     CHAR(8)                             not null,
    LOPENR           SMALLINT                            not null,
    DATO_UGYLDIG_FOM DATE,
    BRUKERID         CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG      TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

DROP TABLE IF EXISTS T_KONTOREGEL;
CREATE TABLE T_KONTOREGEL
(
    KODE_KLASSE         CHAR(20)                            not null,
    DATO_FOM            DATE,
    DATO_TOM            DATE,
    ART_ID              INTEGER                             not null,
    ART_ID_ETTEROPPGJOR INTEGER                             not null,
    AAR                 SMALLINT                            not null,
    HOVEDKONTONR        CHAR(3)                             not null,
    UNDERKONTONR        CHAR(4)                             not null,
    KODE_FORMAL         CHAR(6)                             not null,
    KODE_AKTIVITET      CHAR(6)                             not null,
    BRUKERID            CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG         TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

DROP TABLE IF EXISTS T_VENT_KRITERIUM;
create table T_VENT_KRITERIUM
(
    KODE_FAGGRUPPE     CHAR(8)                             not null,
    TYPE_BILAG         CHAR(2)                             not null,
    DATO_FOM           DATE                                not null,
    BELOP_BRUTTO       DECIMAL(15, 2),
    BELOP_NETTO        DECIMAL(15, 2),
    ANT_DAGER_ELDREENN SMALLINT,
    TIDLIGERE_AAR      CHAR(1)                             not null,
    BRUKERID           CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG        TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

DROP TABLE IF EXISTS T_VENT_STATUSKODE;
create table T_VENT_STATUSKODE
(
    KODE_VENTESTATUS CHAR(4)                             not null,
    BESKRIVELSE      CHAR(40),
    TYPE_VENTESTATUS CHAR(4)                             not null,
    KODE_ARVES_TIL   CHAR(4)                             not null,
    SETTES_MANUELT   CHAR(1)                             not null,
    OVERFOR_MOTTKOMP CHAR(1)                             not null,
    PRIORITET        SMALLINT                            not null,
    TIL_GSAK         CHAR(1)                             not null,
    BRUKERID         CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_REG      TIMESTAMP(6) default CURRENT TIMESTAMP not null
);

DROP TABLE IF EXISTS T_VENT_STATUSREGEL;
create table T_VENT_STATUSREGEL
(
    KODE_VENTESTATUS_H CHAR(4)                             not null,
    KODE_VENTESTATUS_U CHAR(4)                             not null,
    BRUKERID           CHAR(8)      default 'CURRENT USER' not null,
    TIDSPKT_ENDRET     TIMESTAMP(6) default CURRENT TIMESTAMP not null
);